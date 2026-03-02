package com.abdownloadmanager.desktop.integration

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.shared.pages.adddownload.SilentImportOptions
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.integration.HttpDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.NewDownloadTask
import com.abdownloadmanager.integration.ApiQueueModel
import com.abdownloadmanager.integration.AddDownloadOptionsFromIntegration
import com.abdownloadmanager.integration.HLSDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IntegrationHandlerImp : IntegrationHandler, KoinComponent {
    val appComponent by inject<AppComponent>()
    val downloadSystem by inject<DownloadSystem>()
    val queueManager by inject<QueueManager>()
    val appSettings by inject<AppRepository>()
    private val downloaderInUiRegistry by inject<DownloaderInUiRegistry>()

    override suspend fun addDownload(
        list: List<IDownloadCredentialsFromIntegration>,
        options: AddDownloadOptionsFromIntegration,
    ) {
        appComponent.externalCredentialComingIntoApp(
            list.map {
                convertToDownloadSystemCredentials(it)
            },
            options = ImportOptions(
                silentImport = if (options.silentAdd) {
                    SilentImportOptions(
                        silentDownload = options.silentStart
                    )
                } else null
            )
        )
    }

    override fun listQueues(): List<ApiQueueModel> {
        return queueManager.getAll().map { downloadQueue ->
            val queueModel = downloadQueue.getQueueModel()
            ApiQueueModel(id = queueModel.id, name = queueModel.name)
        }
    }
    override suspend fun addDownloadTask(task: NewDownloadTask) {
        val addDownloaderInUiProps = convertToDownloadSystemCredentials(task.downloadSource)
        val downloaderInUi = downloaderInUiRegistry.getDownloaderOf(
            addDownloaderInUiProps.credentials
        ) ?: error("Downloader for ${addDownloaderInUiProps.credentials::class.qualifiedName} not found")
        val downloadItem = downloaderInUi.createBareDownloadItem(
            addDownloaderInUiProps.credentials,
            basicDownloadItem = BasicDownloadItem(
                folder = task.folder ?: appSettings.saveLocation.value,
                name = task.name ?: addDownloaderInUiProps.extraConfig.suggestedName
                ?: task.downloadSource.link.substringAfterLast("/"),
            ),
        )
        val id =
            downloadSystem.addDownload(
                newDownload = NewDownloadItemProps(
                    downloadItem = downloadItem,
                    onDuplicateStrategy = OnDuplicateStrategy.default(),
                    extraConfig = null,
                    context = EmptyContext,
                ),
                queueId = task.queueId,
                categoryId = null
            )
        if (task.queueId != null) {
            val queue = queueManager.getQueue(task.queueId!!)
            queue.start()
        } else {
            downloadSystem.userManualResume(id)
        }
    }

    companion object {
        private fun convertToDownloadSystemCredentials(it: IDownloadCredentialsFromIntegration): AddDownloadCredentialsInUiProps {
            val credentials = when (it) {
                is HttpDownloadCredentialsFromIntegration -> {
                    HttpDownloadCredentials(
                        link = it.link,
                        headers = it.headers,
                        downloadPage = it.downloadPage,
                    )
                }

                is HLSDownloadCredentialsFromIntegration -> {
                    HLSDownloadCredentials(
                        link = it.link,
                        headers = it.headers,
                        downloadPage = it.downloadPage,
                    )
                }
            }
            return AddDownloadCredentialsInUiProps(
                credentials = credentials,
                extraConfig = AddDownloadCredentialsInUiProps.Configs(
                    suggestedName = it.suggestedName,
                )
            )
        }
    }
}
