package com.orcchg.trustwallet.task.domain

import com.orcchg.trustwallet.task.domain.model.Key
import com.orcchg.trustwallet.task.domain.model.TransactionStatus
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * An interface to a transactional key-value store.
 *
 * @visibility
 * Belongs to the Domain Layer, can be exposed to any other Layer.
 */
interface Storage {
    /**
     * Retrieves the current value for a given key.
     */
    suspend fun get(key: Key): Value?

    /**
     * Stores a value for the key. If [value] is null, then the
     * entry for the [key] will be removed, if present. If [key]
     * already exists, its value will be overwritten with [value].
     */
    suspend fun set(key: Key, value: Value?)

    /**
     * Removes the entry for the [key], if present.
     */
    suspend fun delete(key: Key)

    /**
     * Returns total count of keys that have the given [value].
     */
    suspend fun count(value: Value): Long

    /**
     * Clears the entire storage and resets its internal state.
     */
    suspend fun clear()

    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////// TRANSACTIONS API ///////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Opens a new transaction.
     * A transaction can be nested into another transaction.
     */
    fun beginTransaction()

    /**
     * Closes the currently open transaction, applying its
     * results to a transactional key-value store.
     *
     * When called from a nested open transaction, will close that
     * inner transaction, while the outer transaction will remain open.
     */
    suspend fun commitTransaction(): TransactionStatus

    /**
     * Closes the currently open transaction, discarding any of its
     * results, so the transaction key-value store won't change.
     *
     * When called from a nested open transaction, will close that
     * inner transaction, while the outer transaction will remain open.
     */
    suspend fun rollbackTransaction(): TransactionStatus
}
