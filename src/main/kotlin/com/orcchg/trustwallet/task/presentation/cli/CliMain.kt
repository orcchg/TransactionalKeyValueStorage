package com.orcchg.trustwallet.task.presentation.cli

import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    CliHelper().start()
    println("Thank you!")
}
