package com.abdownloadmanager.desktop.cli.gui.exit

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.github.ajalt.clikt.command.SuspendingCliktCommand

class Exit : SuspendingCliktCommand(AppArguments.Commands.EXIT) {
    override suspend fun run() {
        val singleInstance = SingleInstanceManager.get()
        runCatching {
            singleInstance.singleInstanceService().useService {
                it.exit()
            }
        }
    }
}
