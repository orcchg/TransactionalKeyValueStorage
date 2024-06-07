package com.orcchg.trustwallet.task.domain.model

sealed interface TransactionStatus {
    data object Success : TransactionStatus
    data class Failure(val code: Code) : TransactionStatus

    enum class Code {
        /**
         * Attempt to close a transaction that was not opened.
         */
        NoOpenTransaction
    }
}
