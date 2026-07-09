package com.abdownloadmanager.cli

import com.abdownloadmanager.cli.BuildConfig
import com.abdownloadmanager.cli.commands.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.system.exitProcess

private const val DEBUG_MODE = false

class CliApp : CliktCommand(
    name = "abdm-cli",
    help = "AB Download Manager CLI — IPC client for the desktop app",
) {
    init {
        versionOption(BuildConfig.APP_VERSION)
    }

    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            echo("AB Download Manager CLI")
            echo("Use --help to see available commands.")
        }
    }
}

fun main(args: Array<String>) {
    try {
        val app = CliApp().subcommands(
            AddCommand(),
            ListCommand(),
            InfoCommand(),
            PauseCommand(),
            ResumeCommand(),
            RemoveCommand(),
        )

        app.main(args)
        // Force exit — OkHttp non-daemon threads keep JVM alive
        exitProcess(0)
    } catch (e: PrintHelpMessage) {
        exitProcess(0)
    } catch (e: PrintCompletionMessage) {
        exitProcess(0)
    } catch (e: com.github.ajalt.clikt.core.CliktError) {
        // Clikt prints the error message to stderr internally
        exitProcess(64)
    } catch (e: Exception) {
        val term = Terminal()
        term.println((TextColors.red)("Error: ${e.message ?: e::class.simpleName ?: "Unknown error"}"))
        if (DEBUG_MODE) {
            e.printStackTrace()
        }
        exitProcess(1)
    }
}