package com.abdownloadmanager.desktop.cli.gui

import com.abdownloadmanager.desktop.cli.gui.exit.Exit
import com.abdownloadmanager.desktop.cli.gui.integration.Integration
import com.abdownloadmanager.desktop.cli.gui.run.RunGui
import com.abdownloadmanager.desktop.cli.gui.startifnotstarted.StartIfNotStarted
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands

class Gui : SuspendingCliktCommand("gui") {
    init {
        subcommands(RunGui())
        subcommands(Integration())
        subcommands(StartIfNotStarted())
        subcommands(Exit())
    }

    override fun help(context: Context) = "GUI options"
    override suspend fun run() = Unit
}
