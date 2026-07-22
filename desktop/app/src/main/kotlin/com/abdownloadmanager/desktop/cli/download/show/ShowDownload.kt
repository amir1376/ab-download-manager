package com.abdownloadmanager.desktop.cli.download.show

import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.abdownloadmanager.desktop.utils.singleInstance.service.ShowDownloadIPC
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.table.table

class ShowDownload : SuspendingCliktCommand("show") {
    override fun help(context: Context) = "Show download(s)"

    private val ids by argument("ID").long().multiple()
    private val watch by option(
        "--watch",
        help = "Track the download progress"
    ).flag()

    override suspend fun run() {
        if (watch) {
            monitorDownloads()
        } else {
            showOnce()
        }
    }

    private suspend fun monitorDownloads() {
        SingleInstanceManager.get().awokenAppIPCService().getService().useService { service ->
            val flow = service.watchDownload(ids)
            val animation = terminal.animation { downloads: List<ShowDownloadIPC> ->
                table {
                    header {
                        row("ID", "Status", "%", "Name", "Folder")
                    }
                    body {
                        downloads.forEach { download ->
                            row(
                                download.id,
                                download.status.coloredName,
                                download.percent?.toString().orEmpty(),
                                download.name,
                                cyan(download.folder)
                            )
                        }
                    }
                }
            }
            try {
                flow.collect(animation::update)
            } finally {
                animation.stop()
            }
        }
    }

    private suspend fun showOnce() {
        val downloads = SingleInstanceManager.get().awokenAppIPCService().getService().useService {
            it.showDownload(ids)
        }

        if (downloads.isEmpty()) {
            echo(yellow("No downloads found."))
            return
        }

        terminal.println(
            table {
                header {
                    row("ID", "Status", "Name", "Folder")
                }

                body {
                    downloads.forEach { download ->
                        row(
                            download.id,
                            download.status.coloredName,
                            download.name,
                            cyan(download.folder)
                        )
                    }
                }
            }
        )
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
