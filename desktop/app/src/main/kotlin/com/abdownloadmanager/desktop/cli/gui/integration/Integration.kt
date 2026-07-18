package com.abdownloadmanager.desktop.cli.gui.integration

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands

class Integration : SuspendingCliktCommand("integration") {
    init {
        subcommands(
            ShowIntegrationPort()
        )
    }

    override suspend fun run() = Unit
}
