package com.orcchg.trustwallet.task.data

import com.orcchg.trustwallet.task.data.local.Database
import com.orcchg.trustwallet.task.data.local.model.keyToDto
import com.orcchg.trustwallet.task.data.local.model.valueToDto
import com.orcchg.trustwallet.task.data.model.*
import com.orcchg.trustwallet.task.domain.Storage
import com.orcchg.trustwallet.task.domain.model.Key
import com.orcchg.trustwallet.task.domain.model.TransactionStatus
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * An implementation of a transactional key-value store given by [Storage].
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 *
 * @concurrency
 * Not thread safe: requires additional synchronization while being accessed
 * from the multithreaded environment.
 */
internal class RealStorage(
    private val database: Database
) : Storage {
    /**
     * Linked list of the open transactions. Head is always the most
     * nested of the currently open transactions, and tail is the most
     * outer parent (top transaction).
     *
     * Each transaction references to its parent transaction.
     *
     * When [transaction] is null, we are outside any transaction, in other
     * words, there are no open transactions at the moment.
     */
    private var transaction: Transaction? = null

    override suspend fun get(key: Key): Value? {
        var currentTransaction = transaction
        while (currentTransaction != null) {
            /**
             * When in a nested transaction, look for operations under
             * the [key] along the linked list of nested transactions till
             * its head. Every transaction may contain the most recent
             * value for the [key] or nothing, if there were no changes for
             * the [key].
             */
            currentTransaction.get(key = key)?.let { operation ->
                when (operation) {
                    is Operation.Delete -> return null // key was deleted in this transaction
                    is Operation.Set -> return operation.value // key has changed in this transaction
                }
            }
                ?: run {
                    currentTransaction = currentTransaction!!.parent
                }
        }
        // there were no changes for the key, get original value from the database
        return database.get(key = key.keyToDto())
    }

    override suspend fun set(key: Key, value: Value?) {
        transaction?.Set(key = key, value = value)
            ?: performSet(key = key, value = value) // outside transaction
    }

    override suspend fun delete(key: Key) {
        transaction?.Delete(key = key)
            ?: performDelete(key = key) // outside transaction
    }

    override suspend fun count(value: Value): Long {
        var snapshot: Map<Key, Operation> = mapOf()
        var currentTransaction = transaction
        while (currentTransaction != null) {
            /**
             * When in a nested transaction, apply its operations on top of
             * its parent transaction, then memorize the result and move on
             * to the parent transaction. Repeat the process until reaching
             * the most parent transaction. This process will obtain a complete
             * snapshot that contains all operations applied.
             */
            snapshot = currentTransaction.snapshot().applyOnTop(onTop = snapshot)
            currentTransaction = currentTransaction.parent
        }

        /**
         * Find interesting keys in the database, whose values are equal to [value].
         * Convert it to a map of transactional operations for convenience, then
         * apply resulting snapshot on top of it, then calculate final count of unique
         * keys that have value equal to [value].
         */
        return database.findByValue(value = value.valueToDto())
            .mapValues { (key, value) -> Transaction().Set(key = key, value = value) }
            .applyOnTop(onTop = snapshot)
            .mapValues { (_, operation) ->
                when (operation) {
                    is Operation.Delete -> null // no value for deleted key
                    is Operation.Set -> operation.value
                }
            }
            .count { (_, v) -> v == value }.toLong()
    }

    override suspend fun clear() {
        database.clear()
        transaction = null
    }

    override fun beginTransaction() {
        /**
         * Creates a new transaction and inserts it at the head of the
         * linked list of currently open transactions.
         */
        val newTransaction = Transaction(parent = transaction)
        transaction = newTransaction
    }

    override suspend fun commitTransaction(): TransactionStatus =
        transaction
            ?.let { transactionToCommit ->
                transactionToCommit.parent
                    ?.let { parentTransaction ->
                        /**
                         * When in a nested transaction, its operations
                         * should be merged into its parent transaction, and
                         * this transaction will then pop.
                         */
                        parentTransaction.merge(transactionToCommit)
                        transaction = parentTransaction
                    }
                    ?: run {
                        /**
                         * Commit all operations from the top transaction
                         * directly to the database.
                         */
                        transactionToCommit.snapshot().forEach { (key, operation) ->
                            when (operation) {
                                is Operation.Delete -> performDelete(key = key)
                                is Operation.Set -> performSet(key = key, value = operation.value)
                            }
                        }
                        transaction = null
                    }
                TransactionStatus.Success
            }
            ?: TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction)

    override suspend fun rollbackTransaction(): TransactionStatus =
        /**
         * Pops the most nested transaction as it's been rolled back
         * just now, all its operations will be ignored in any further calls.
         * Move on to its parent transaction.
         */
        transaction
            ?.let {
                transaction = it.parent
                TransactionStatus.Success
            }
            ?: TransactionStatus.Failure(code = TransactionStatus.Code.NoOpenTransaction)

    private suspend fun performDelete(key: Key) = database.delete(key = key.keyToDto())

    private suspend fun performSet(key: Key, value: Value?) =
        database.set(key = key.keyToDto(), value = value?.valueToDto())
}
