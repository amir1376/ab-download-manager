package com.abdownloadmanager.desktop.cli.download.add

import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.abdownloadmanager.desktop.utils.singleInstance.service.AddDownloadFromIPC
import com.abdownloadmanager.integration.AddDownloadOptionsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.NewDownloadTask
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import ir.amirab.util.FileNameValidator

abstract class BaseNewDownload(
    name: String
) : SuspendingCliktCommand(name) {
    val outputDir by option("--folder")
        .file(
            canBeFile = false
        )
    val name by option("--name")
        .validate { FileNameValidator.isValidFileName(it) }
    val downloadPage by option("--download-page")
        .validate { FileNameValidator.isValidFileName(it) }

    val start by option("--start").flag()
    val category by option("--category").long()
    val queue by option("--queue").long()

    override suspend fun run() {
        SingleInstanceManager.get().appIPCService().useService {
            it.addDownload(
                AddDownloadFromIPC(
                    items = listOf(
                        NewDownloadTask(
                            downloadSource = createDownload(),
                            folder = outputDir?.path,
                            name = name,
                            queueId = queue,
                            categoryId = category,
                        )
                    ),
                    options = AddDownloadOptionsFromIntegration(
                        silentAdd = true,
                        silentStart = start,
                    )
                )
            )
        }
    }

    abstract fun createDownload(): IDownloadCredentialsFromIntegration
}
