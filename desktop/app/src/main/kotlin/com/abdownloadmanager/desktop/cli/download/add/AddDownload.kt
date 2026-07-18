package com.abdownloadmanager.desktop.cli.download.add

import com.abdownloadmanager.desktop.cli.download.add.hls.NewHlsDownload
import com.abdownloadmanager.desktop.cli.download.add.http.NewHttpDownload
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands

class AddDownload : SuspendingCliktCommand("add") {
    override fun help(context: Context) = "Add new download"

    init {
        subcommands(
            NewHttpDownload(),
            NewHlsDownload(),
        )
    }

    override suspend fun run() = Unit
}
