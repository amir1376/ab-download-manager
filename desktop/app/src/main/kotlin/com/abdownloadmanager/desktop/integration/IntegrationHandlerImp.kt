package com.abdownloadmanager.desktop.integration

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.utils.DownloadSystem
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.integration.NewDownloadInfoFromIntegration
import com.abdownloadmanager.integration.NewDownloadTask
import com.abdownloadmanager.integration.ApiQueueModel
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IntegrationHandlerImp: IntegrationHandler,KoinComponent{
    val appComponent by inject<AppComponent>()
    val downloadSystem by inject<DownloadSystem>()
    val queueManager by inject<QueueManager>()
    val appSettings by inject<AppRepository>()
    override suspend fun addDownload(list: List<NewDownloadInfoFromIntegration>) {
        appComponent.externalCredentialComingIntoApp(list.map {
            DownloadCredentials(
                link = it.link,
                headers = it.headers,
                downloadPage = it.downloadPage,
            )
        })
    }
    override fun listQueues(): List<ApiQueueModel> {
        return queueManager.getAll().map { downloadQueue ->
            val queueModel = downloadQueue.getQueueModel()
            ApiQueueModel(id = queueModel.id, name = queueModel.name)
        }
    }
    override suspend fun addDownloadTask(task: NewDownloadTask) {
        val downloadItem =
                DownloadItem(
                        link = task.downloadSource.link,
                        headers = task.downloadSource.headers,
                        downloadPage = task.downloadSource.downloadPage,
                        folder = task.folder ?: appSettings.saveLocation.value,
                        id = -1,
                        name = task.name ?: task.downloadSource.link.substringAfterLast("/"),
                )
        val id =
                downloadSystem.addDownload(
                    downloadItem = downloadItem,
                    onDuplicateStrategy = OnDuplicateStrategy.default(),
                    queueId = task.queueId,
                    categoryId = null
                )
        if (task.queueId != null) {
            val queue = queueManager.getQueue(task.queueId!!)
            queue.start()
        } else {
            downloadSystem.manualResume(id)
        }
    }
}
