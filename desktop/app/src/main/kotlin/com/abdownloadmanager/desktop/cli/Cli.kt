package com.abdownloadmanager.desktop.cli

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.cli.download.Download
import com.abdownloadmanager.desktop.cli.gui.Gui
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.AppProperties
import com.abdownloadmanager.desktop.utils.isInDebugMode
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import ir.amirab.util.logger.AppLogger

class Cli : SuspendingCliktCommand("ABDownloadManagerCli") {

    val debug by option(AppArguments.Args.DEBUG).flag()

    init {
        versionOption(AppInfo.version.toString())
        subcommands(
            Gui(),
            Download(),
        )
    }

    override suspend fun run() {
        AppArguments.update {
            it.copy(
                debug = debug,
            )
        }
        AppProperties.boot()
        AppLogger.init(
            writeToConsole = false,
            logFilePath = AppInfo.definedPaths.logDir.takeIf {
                AppInfo.isInDebugMode()
            },
        )
    }
}
