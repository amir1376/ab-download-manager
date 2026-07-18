package com.abdownloadmanager.desktop.cli

import com.abdownloadmanager.desktop.cli.download.Download
import com.abdownloadmanager.desktop.utils.AppInfo
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption

class Cli : SuspendingCliktCommand("ABDownloadManagerCli") {
    init {
        versionOption(AppInfo.version.toString())
        subcommands(
            Download()
        )
    }

    override suspend fun run() = Unit
}
