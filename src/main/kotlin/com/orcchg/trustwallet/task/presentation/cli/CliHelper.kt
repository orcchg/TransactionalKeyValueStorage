package com.orcchg.trustwallet.task.presentation.cli

import com.orcchg.trustwallet.task.data.RealStorage
import com.orcchg.trustwallet.task.data.local.InMemoryDatabase
import com.orcchg.trustwallet.task.domain.Storage
import com.orcchg.trustwallet.task.domain.model.TransactionStatus
import com.orcchg.trustwallet.task.domain.model.Value

/**
 * CLI for the transactional key-value storage. Supports the following interface:
 *
 * SET <key> <value>   : stores the value for the key
 * GET <key>           : returns the current value for the key
 * DELETE <key>        : removes the entry for the key
 * COUNT <value>       : returns the number of keys that have the given value
 * BEGIN               : starts a new transaction
 * COMMIT              : completes the current transaction
 * ROLLBACK            : reverts to a state prior to BEGIN call
 * HELP                : displays usage
 * EXIT                : gracefully stops the process and exits
 */
class CliHelper {
    private val storage: Storage by lazy(LazyThreadSafetyMode.NONE) { RealStorage(database = InMemoryDatabase()) }
    private val whitespace by lazy(LazyThreadSafetyMode.NONE) { WHITESPACE.toRegex() }

    suspend fun start() {
        displayHelp()
        while (true) {
            try {
                readlnOrNull()
                    ?.trim()
                    ?.split(whitespace)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { content ->
                        val command = try {
                            content.firstOrNull()?.uppercase()?.let(CliKeyword::valueOf)
                        } catch (e: IllegalArgumentException) {
                            CliKeyword.UNKNOWN // allow multiple newlines
                        }
                        when (command) {
                            CliKeyword.BEGIN -> storage.beginTransaction()
                            CliKeyword.COMMIT -> storage.commitTransaction().also(::displayTransactionStatus)
                            CliKeyword.COUNT -> storage.count(value = content[1]).also(::displayResult)
                            CliKeyword.DELETE -> storage.delete(key = content[1])
                            CliKeyword.EXIT -> return
                            CliKeyword.GET -> storage.get(key = content[1]).also(::displayGetResult)
                            CliKeyword.HELP -> displayHelp()
                            CliKeyword.ROLLBACK -> storage.rollbackTransaction().also(::displayTransactionStatus)
                            CliKeyword.SET -> storage.set(key = content[1], value = content[2])
                            CliKeyword.UNKNOWN -> { /* don't warn */ }
                            else -> warn("invalid command: $command")
                        }
                    }
            } catch (e: Throwable) {
                error("$e")
            }
        }
    }

    private fun displayHelp() {
        println("""
            
            Welcome to a Key-Value Storage !
            Please use the following commands for input:
            
             * SET <key> <value>   : stores the value for the key
             * GET <key>           : returns the current value for the key
             * DELETE <key>        : removes the entry for the key
             * COUNT <value>       : returns the number of keys that have the given value
             * BEGIN               : starts a new transaction
             * COMMIT              : completes the current transaction
             * ROLLBACK            : reverts to a state prior to BEGIN call
             * HELP                : displays usage
             * EXIT                : gracefully stops the process and exits
             
        """.trimIndent())
    }

    private fun displayGetResult(result: Any?) {
        result?.let(::println) ?: println("key not set")
    }

    private fun displayResult(result: Any?) {
        result?.let(::println)
    }

    private fun displayTransactionStatus(status: TransactionStatus) {
        when (status) {
            is TransactionStatus.Failure ->
                when (status.code) {
                    TransactionStatus.Code.NoOpenTransaction -> println("no transaction")
                }
            else -> { /* no-op */ }
        }
    }

    private fun error(msg: String) {
        println("ERROR: $msg")
    }

    private fun warn(msg: String) {
        println("WARNING: $msg")
    }

    companion object {
        private const val UNKNOWN = ""
        private const val WHITESPACE = "\\s+"
    }
}
