package com.abdownloadmanager.desktop.cli

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.cli.download.Download
import com.abdownloadmanager.desktop.cli.gui.Gui
import com.abdownloadmanager.desktop.cli.nativemessaging.NativeMessagingCommand
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.AppProperties
import com.abdownloadmanager.desktop.utils.EntryType
import com.abdownloadmanager.desktop.utils.EntrypointInitializer
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
            NativeMessagingCommand(),
        )
    }

    override suspend fun run() {
        EntrypointInitializer.boot(
            debug = debug,
            entryType = EntryType.CLI,
        )
    }

}
