package com.orcchg.trustwallet.task.data.local.model

import com.orcchg.trustwallet.task.data.local.Database
import com.orcchg.trustwallet.task.domain.model.Key

/**
 * A special type to represent a key stored in the [Database].
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 *
 * @extensibility
 * When extended to a non-trivial class, must be 'data class' or define
 * a correct equals contract.
 */
typealias DatabaseKey = String

/**
 * Converts [Key] from the Domain Layer to [DatabaseKey] in the Data Layer.
 */
fun Key.keyToDto(): DatabaseKey = this
fun DatabaseKey.keyToDomain(): Key = this
