package com.abdownloadmanager.desktop.integration

import com.abdownloadmanager.desktop.AppComponent
import ir.amirab.downloader.downloaditem.IDownloadItem
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.shared.pages.adddownload.SilentImportOptions
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.integration.HttpDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.NewDownloadTask
import com.abdownloadmanager.integration.ApiDownloadModel
import com.abdownloadmanager.integration.ApiQueueModel
import com.abdownloadmanager.integration.AddDownloadOptionsFromIntegration
import com.abdownloadmanager.integration.HLSDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.util.category.CategoryItemWithId
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.DownloadManager
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
    override suspend fun addDownloadTask(task: NewDownloadTask): Long {
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
        // Use downloadManager directly to get the ID first
        val id = downloadSystem.downloadManager.addDownload(
            NewDownloadItemProps(
                downloadItem = downloadItem,
                onDuplicateStrategy = OnDuplicateStrategy.default(),
                extraConfig = null,
                context = EmptyContext,
            )
        )
        // Assign to queue if specified
        task.queueId?.let {
            queueManager.addToQueue(it, id)
        }
        // Auto-categorize by file extension & name
        downloadSystem.categoryManager.autoAddItemsToCategoriesBasedOnFileNames(
            listOf(
                CategoryItemWithId(
                    id = id,
                    fileName = downloadItem.name,
                    url = downloadItem.link,
                )
            )
        )
        // Start the download
        if (task.queueId != null) {
            queueManager.getQueue(task.queueId!!).start()
        } else {
            downloadSystem.userManualResume(id)
        }
        return id
    }

    override suspend fun listDownloads(): List<ApiDownloadModel> {
        return downloadSystem.getDownloadItemsBy { true }.map { it.toApiModel() }
    }

    override suspend fun getDownloadInfo(id: Long): ApiDownloadModel? {
        return downloadSystem.getDownloadItemById(id)?.toApiModel()
    }

    override suspend fun pauseDownloads(ids: List<Long>) {
        ids.forEach { downloadSystem.manualPause(it) }
    }

    override suspend fun resumeDownloads(ids: List<Long>) {
        ids.forEach { downloadSystem.userManualResume(it) }
    }

    override suspend fun removeDownloads(ids: List<Long>, keepFile: Boolean) {
        ids.forEach { downloadSystem.removeDownload(it, !keepFile, EmptyContext) }
    }

    companion object {
        private fun IDownloadItem.toApiModel() = ApiDownloadModel(
            id = id,
            name = name,
            url = link,
            folder = folder,
            status = status.name,
            size = contentLength,
            downloaded = 0,
            speed = 0,
            progress = 0.0,
            dateAdded = dateAdded,
            startTime = startTime,
            completeTime = completeTime,
            connections = preferredConnectionCount,
            speedLimit = speedLimit,
            checksum = fileChecksum,
        )

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
