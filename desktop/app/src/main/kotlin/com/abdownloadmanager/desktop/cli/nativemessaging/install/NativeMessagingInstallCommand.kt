package com.abdownloadmanager.desktop.cli.nativemessaging.install

import com.abdownloadmanager.desktop.nativemessaging.NativeMessaging
import com.abdownloadmanager.desktop.nativemessaging.host.NativeMessagingHostLauncher
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.PrintMessage

class NativeMessagingInstallCommand : SuspendingCliktCommand(
    "install"
) {
    override fun help(context: Context): String = "Installs the native messaging host manifest file"

    override suspend fun run() {
        NativeMessaging.getDefault().installManifests()
    }
}
