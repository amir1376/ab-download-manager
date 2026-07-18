package com.abdownloadmanager.desktop.cli.download.add

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context

class AddDownload : SuspendingCliktCommand("add") {
    override fun help(context: Context) = "Add new download"
    override suspend fun run() = Unit
}
