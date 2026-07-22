package com.abdownloadmanager.desktop.utils.singleInstance.service

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceInitialized
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.integration.model.AddDownloadsFromIntegration
import com.abdownloadmanager.integration.model.ApiQueueModel
import com.abdownloadmanager.integration.model.NewDownloadTask
import ir.amirab.downloader.downloaditem.contexts.RemovedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAppIPCServiceImpl : IDefaultAppIPCService, KoinComponent {
    private suspend fun awaitAppBoot() {
        SingleInstanceInitialized.awaitDone()
    }

    private val appComponent: AppComponent by inject()
    private val downloadSystem get() = appComponent.downloadSystem
    private val integrationHandler: IntegrationHandler by inject()

    override suspend fun addDownloadByGui(request: AddDownloadsFromIntegration) {
        integrationHandler.addDownloadByGui(request)
    }

    override suspend fun addDownload(request: NewDownloadTask): Long {
        awaitAppBoot()
        return integrationHandler.addDownload(request)
    }

    override suspend fun pauseDownload(ids: List<Long>) {
        awaitAppBoot()
        for (id in ids) {
            downloadSystem.manualPause(id)
        }
    }

    override suspend fun resumeDownload(ids: List<Long>) {
        awaitAppBoot()
        for (id in ids) {
            downloadSystem.userManualResume(id)
        }
    }

    override suspend fun removeDownload(ids: List<Long>, alsoRemoveFile: Boolean) {
        awaitAppBoot()
        for (id in ids) {
            downloadSystem.removeDownload(id, alsoRemoveFile, RemovedBy(User))
        }
    }

    override suspend fun showDownload(ids: List<Long>): List<ShowDownloadIPC> {
        awaitAppBoot()

        return ids.mapNotNull {
            runCatching {
                downloadSystem.getDownloadItemById(it)
            }.getOrNull()
        }.map {
            ShowDownloadIPC(
                name = it.name,
                folder = it.folder,
                id = it.id,
                status = ShowDownloadIPC.DownloadStatus.fromDownloadStatus(
                    it.status
                ),
                percent = null
            )
        }
    }

    override fun watchDownload(ids: List<Long>): Flow<List<ShowDownloadIPC>> {
        return flow {
            downloadSystem.downloadMonitor.downloadListFlow.collect { downloads ->
                val byId = downloads.associateBy { it.id }
                val toBeSend = ids
                    .mapNotNull(byId::get)
                    .map {
                        ShowDownloadIPC(
                            id = it.id,
                            name = it.name,
                            folder = it.folder,
                            status = ShowDownloadIPC.DownloadStatus.fromDownloadStatus(
                                it.statusOrFinished()
                            ),
                            percent = when (it) {
                                is ProcessingDownloadItemState -> it.percent
                                is CompletedDownloadItemState -> 100
                            }
                        )
                    }
                emit(toBeSend)
            }
        }
    }

    override suspend fun listQueues(): List<ApiQueueModel> {
        awaitAppBoot()
        return downloadSystem.queueManager.queues.value
            .asSequence()
            .map { it.queueModel.value }
            .map { ApiQueueModel(it.id, it.name) }
            .toList()
    }

}
