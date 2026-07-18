package com.abdownloadmanager.desktop.cli.nativemessaging

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.utils.native_messaging.host.NativeMessagingHostLauncher
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import kotlin.system.exitProcess

class NativeMessagingCommand : SuspendingCliktCommand(
    AppArguments.Commands.NATIVE_MESSAGING
) {
    override fun help(context: Context): String = "Native messaging mode"
    override suspend fun run() {
        runNativeMessagingMode(emptyArray())
    }
}

/**
 * Runs the app in native messaging mode for browser extension communication.
 * This mode runs without UI and communicates via stdin/stdout.
 */
private fun runNativeMessagingMode(args: Array<String>): Nothing {
    val nmArgs = args.drop(1).toTypedArray() // nativeMessaging command
    NativeMessagingHostLauncher.main(nmArgs)
    exitProcess(0)
}
