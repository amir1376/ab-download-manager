package com.abdownloadmanager.desktop.cli.nativemessaging.uninstall

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.nativemessaging.NativeMessaging
import com.abdownloadmanager.desktop.nativemessaging.host.NativeMessagingHostLauncher
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context

class NativeMessagingUninstallCommand : SuspendingCliktCommand(
    "uninstall"
) {

    override fun help(context: Context): String = "Uninstalls the native messaging host manifest file"

    override suspend fun run() {
        NativeMessaging.getDefault().uninstallManifests()
    }
}
