package com.abdownloadmanager.desktop.cli.gui.startifnotstarted

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.isInIDE
import com.abdownloadmanager.desktop.utils.singleInstance.StartIfNotStartedCommand
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.PrintMessage

class StartIfNotStarted : SuspendingCliktCommand(
    AppArguments.Commands.START_IF_NOT_STARTED
) {
    override suspend fun run() {
        if (AppInfo.isInIDE()) {
            throw PrintMessage(
                "we can't start the app, because the command executed from the IDE",
                statusCode = 1,
                printError = true
            )
        }
        val result = StartIfNotStartedCommand.startAndWaitForRunIfNotRunning()
        val isSuccessFull = result.isSuccessful()
        throw PrintMessage(
            if (isSuccessFull) {
                result.toString()
            } else {
                "we can't start the app: $result"
            },
            statusCode = if (isSuccessFull) 0 else 1,
            printError = !isSuccessFull
        )
    }

}
