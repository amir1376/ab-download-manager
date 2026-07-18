package com.abdownloadmanager.desktop.cli.download

import com.abdownloadmanager.desktop.cli.download.add.AddDownload
import com.abdownloadmanager.desktop.cli.download.pause.PauseDownload
import com.abdownloadmanager.desktop.cli.download.remove.RemoveDownload
import com.abdownloadmanager.desktop.cli.download.resume.ResumeDownload
import com.abdownloadmanager.desktop.cli.download.show.ShowDownload
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands

class Download : SuspendingCliktCommand("download") {
    init {
        subcommands(
            AddDownload(),
            ShowDownload(),
            RemoveDownload(),
            PauseDownload(),
            ResumeDownload(),
        )
    }

    override fun help(context: Context) = "Commands related to downloads"
    override suspend fun run() = Unit
}
