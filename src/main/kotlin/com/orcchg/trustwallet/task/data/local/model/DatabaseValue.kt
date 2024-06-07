package com.orcchg.trustwallet.task.data.local.model

import com.orcchg.trustwallet.task.data.local.Database
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * A special type to represent a value stored in the [Database].
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 *
 * @extensibility
 * When extended to a non-trivial class, must be 'data class' or define
 * a correct equals contract.
 */
typealias DatabaseValue = String

/**
 * Converts [Value] from the Domain Layer to [DatabaseValue] in the Data Layer.
 */
fun Value.valueToDto(): DatabaseValue = this
fun DatabaseValue.valueToDomain(): Value = this
