package com.abdownloadmanager.desktop.cli.download.add.shared

import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.abdownloadmanager.integration.model.IDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.model.NewDownloadTask
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import ir.amirab.util.FileNameValidator
import ir.amirab.util.HttpUrlUtils

abstract class BaseNewDownload(
    name: String
) : SuspendingCliktCommand(name) {
    val outputDir by option("--folder")
        .file(canBeFile = false)
    val name by option("--name")
        .validate { FileNameValidator.isValidFileName(it) }
    val downloadPage by option("--download-page")
        .validate { HttpUrlUtils.isValidUrl(it) }

    val start by option(
        "--start",
        help = "also start the download"
    ).flag()
    val startQueue by option(
        "--start-queue",
        help = "also start the download queue"
    ).flag()
    val category by option(
        "--category",
    ).long()
    val queue by option(
        "--queue",
    ).long()

    override suspend fun run() {
        val id = SingleInstanceManager.get().awokenAppIPCService().getService().useService {
            it.addDownload(
                NewDownloadTask(
                    downloadSource = createDownload(),
                    folder = outputDir?.path,
                    name = name,
                    queueId = queue,
                    categoryId = category,
                    startDownload = start,
                    startQueue = startQueue
                ),
            )
        }
        throw PrintCompletionMessage(id.toString())
    }

    abstract fun createDownload(): IDownloadCredentialsFromIntegration


    // utilities
    fun ParameterHolder.headersOption() = option("--header", "-h")
        .convert {
            val (key, value) = it.split('=', ':')
            key to value
        }
        .multiple()

    fun ParameterHolder.linkOption() = option("--link")
        .required()
        .validate { HttpUrlUtils.isValidUrl(it) }
}
