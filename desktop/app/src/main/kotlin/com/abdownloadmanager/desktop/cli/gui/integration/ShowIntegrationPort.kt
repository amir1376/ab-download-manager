package com.abdownloadmanager.desktop.cli.gui.integration

import com.abdownloadmanager.desktop.utils.IntegrationPortBroadcaster
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context

class ShowIntegrationPort : SuspendingCliktCommand("show") {
    override fun help(context: Context) = "Show integration port and exit"
    override suspend fun run() {
        val singleInstance = SingleInstanceManager.get()

        val port = runCatching {
            singleInstance.singleInstanceService().useService { it.getIntegrationPort() }
        }.getOrElse { IntegrationPortBroadcaster.INTEGRATION_UNKNOWN }
        echo(port)
    }
}
