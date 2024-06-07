package com.orcchg.trustwallet.task.data.model

import com.orcchg.trustwallet.task.domain.model.Key
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * Represents an internal operation for a transaction snapshot.
 *
 * @visibility
 * Belongs to the Data Layer, should not be exposed to any other Layer.
 */
internal sealed interface Operation {
    val key: Key

    data class Delete(override val key: Key) : Operation
    data class Set(override val key: Key, val value: Value?) : Operation
}
