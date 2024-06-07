package com.orcchg.trustwallet.task.data.local

import com.orcchg.trustwallet.task.data.local.model.DatabaseKey
import com.orcchg.trustwallet.task.data.local.model.DatabaseValue

/**
 * An internal interface to the database.
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 */
interface Database {
    suspend fun size(): Long
    suspend fun get(key: DatabaseKey): DatabaseValue?
    suspend fun set(key: DatabaseKey, value: DatabaseValue?)
    suspend fun count(value: DatabaseValue): Long
    suspend fun findByValue(value: DatabaseValue): Map<DatabaseKey, DatabaseValue>
    suspend fun delete(key: DatabaseKey)
    suspend fun clear()
}
