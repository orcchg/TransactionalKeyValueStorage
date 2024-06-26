package com.orcchg.trustwallet.task.data.model

import com.orcchg.trustwallet.task.domain.model.Key
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * Represents a single transaction.
 *
 * Maintains a snapshot of the storage with the operations
 * applied within this transaction.
 *
 * Keeps a reference to a parent transaction, if nested.
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 */
internal data class Transaction(
    var parent: Transaction? = null,
    private val snapshot: MutableMap<Key, Operation> = mutableMapOf()
) {
    fun addOperation(operation: Operation) {
        snapshot[operation.key] = operation
    }

    fun get(key: Key): Operation? = snapshot[key]

    fun merge(other: Transaction) {
        snapshot.putAll(other.snapshot)
    }

    fun snapshot(): Map<Key, Operation> = snapshot
}

internal fun Transaction.Delete(key: Key): Operation.Delete =
    Operation.Delete(key = key).also(this::addOperation)

internal fun Transaction.Set(key: Key, value: Value?): Operation.Set =
    Operation.Set(key = key, value = value).also(this::addOperation)

/**
 * Applies [onTop] map on top of this map, replacing or removing corresponding keys,
 * or adding new keys, if any. Returns the new map that contains final result of
 * the application.
 *
 * This function doesn't modify its arguments.
 */
internal fun Map<Key, Operation>.applyOnTop(onTop: Map<Key, Operation>): Map<Key, Operation> {
    val resultMap = this.toMutableMap() // copy of this map
    onTop.forEach { (key, operation) -> resultMap[key] = operation }
    return resultMap
}
