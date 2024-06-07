package com.orcchg.trustwallet.task.data.local

import com.orcchg.trustwallet.task.data.local.model.DatabaseKey
import com.orcchg.trustwallet.task.data.local.model.DatabaseValue

/**
 * An in-memory implementation for the [Database].
 *
 * @concurrency
 * Not thread safe: requires additional synchronization while being accessed
 * from the multithreaded environment.
 */
internal class InMemoryDatabase : Database {
    private val storage = mutableMapOf<DatabaseKey, DatabaseValue>()

    override suspend fun size(): Long = storage.size.toLong()

    override suspend fun get(key: DatabaseKey): DatabaseValue? = storage[key]

    override suspend fun set(key: DatabaseKey, value: DatabaseValue?) {
        value
            ?.let { storage[key] = value }
            ?: storage.remove(key)
    }

    override suspend fun count(value: DatabaseValue): Long =
        storage.count { (_, v) -> v == value }.toLong()

    override suspend fun findByValue(value: DatabaseValue): Map<DatabaseKey, DatabaseValue> =
        storage.filterValues { it == value }

    override suspend fun delete(key: DatabaseKey) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }
}
