package com.abdownloadmanager.desktop.cli

import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.runBlocking


class CliApp {
    suspend fun run(args: Array<String>) {
        Cli().main(args)
    }
}

fun main(args: Array<String>) {
    runBlocking {
        CliApp().run(args)
    }
}
