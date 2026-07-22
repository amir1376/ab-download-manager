package com.abdownloadmanager.desktop.cli.nativemessaging

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.cli.nativemessaging.install.NativeMessagingInstallCommand
import com.abdownloadmanager.desktop.cli.nativemessaging.run.NativeMessagingRunRunCommand
import com.abdownloadmanager.desktop.cli.nativemessaging.uninstall.NativeMessagingUninstallCommand
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands

class NativeMessagingCommand : SuspendingCliktCommand(
    "native-messaging"
) {

    init {
        subcommands(
            NativeMessagingInstallCommand(),
            NativeMessagingUninstallCommand(),
            NativeMessagingRunRunCommand(),
        )
    }

    override suspend fun run() = Unit
}

