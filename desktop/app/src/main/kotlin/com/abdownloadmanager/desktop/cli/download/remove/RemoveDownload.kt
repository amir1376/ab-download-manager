package com.abdownloadmanager.desktop.cli.download.remove

import arrow.core.raise.option
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long

class RemoveDownload : SuspendingCliktCommand("remove") {
    override fun help(context: Context) = "remove download(s)"
    val id by argument("id").long().multiple()
    val removeFiles by option("--remove-file").flag()
    override suspend fun run() {
        SingleInstanceManager.get().appIPCService().useService {
            it.removeDownload(id, removeFiles)
        }
    }
}
