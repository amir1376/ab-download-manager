package com.abdownloadmanager.desktop.cli.download.show

import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.abdownloadmanager.desktop.utils.singleInstance.service.ShowDownloadIPC
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*

class ShowDownload : SuspendingCliktCommand("show") {
    override fun help(context: Context) = "Show download(s)"

    private val ids by argument("ID").long().multiple()

    override suspend fun run() {
        val downloads = SingleInstanceManager.get().appIPCService().useService {
            it.showDownload(ids)
        }

        if (downloads.isEmpty()) {
            echo(yellow("No downloads found."))
            return
        }

        printDownloads(downloads)
    }

    private fun printDownloads(downloads: List<ShowDownloadIPC>) {
        val idWidth = maxOf(2, downloads.maxOf { it.id.toString().length })
        val statusWidth = maxOf("STATUS".length, downloads.maxOf { it.status.displayName.length })

        echo(
            bold(
                "%-${idWidth}s  %-${statusWidth}s  %-4s  %s".format(
                    "ID",
                    "STATUS",
                    "%",
                    "NAME"
                )
            )
        )

        downloads.forEach { download ->
            echo(
                "%-${idWidth}d  %-${statusWidth}s  %3d%%  %s".format(
                    download.id,
                    download.status.coloredName,
                    download.percent,
                    download.name
                )
            )

            echo(" ".repeat(idWidth + statusWidth + 8) + cyan(download.folder))
        }
    }

    private val ShowDownloadIPC.DownloadStatus.displayName: String
        get() = when (this) {
            ShowDownloadIPC.DownloadStatus.Paused -> "Paused"
            ShowDownloadIPC.DownloadStatus.Error -> "Error"
            ShowDownloadIPC.DownloadStatus.Downloading -> "Downloading"
            ShowDownloadIPC.DownloadStatus.Finished -> "Finished"
            ShowDownloadIPC.DownloadStatus.PreparingFile -> "PreparingFile"
            ShowDownloadIPC.DownloadStatus.Resuming -> "Resuming"
            ShowDownloadIPC.DownloadStatus.Retrying -> "Retrying"
        }

    private val ShowDownloadIPC.DownloadStatus.coloredName: String
        get() = when (this) {
            ShowDownloadIPC.DownloadStatus.PreparingFile,
            ShowDownloadIPC.DownloadStatus.Resuming,
            ShowDownloadIPC.DownloadStatus.Downloading -> cyan(displayName)

            ShowDownloadIPC.DownloadStatus.Retrying,
            ShowDownloadIPC.DownloadStatus.Paused -> yellow(displayName)

            ShowDownloadIPC.DownloadStatus.Finished -> green(displayName)
            ShowDownloadIPC.DownloadStatus.Error -> red(displayName)
        }
}
