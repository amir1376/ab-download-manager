package com.abdownloadmanager.desktop.utils.singleInstance.service

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.integration.IntegrationHandlerImp.Companion.convertToDownloadSystemCredentials
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceServerInitializer
import com.abdownloadmanager.integration.ApiQueueModel
import com.abdownloadmanager.shared.downloaderinui.BasicDownloadItem
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.downloaditem.contexts.RemovedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.utils.OnDuplicateStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAppIPCServiceImpl : IDefaultAppIPCService, KoinComponent {
    private suspend fun awaitAppBoot() {
        SingleInstanceServerInitializer.booted.awaitDone()
    }

    private val appComponent: AppComponent by inject()
    private val appSettings get() = appComponent.appRepository
    private val downloadSystem get() = appComponent.downloadSystem
    val downloaderInUiRegistry get() = appComponent.downloaderInUiRegistry

    override suspend fun addDownload(request: AddDownloadFromIPC) {
        awaitAppBoot()
        val items = request.items.map { item ->
            val addDownloaderInUiProps = convertToDownloadSystemCredentials(item.downloadSource)
            val downloaderInUi = downloaderInUiRegistry.getDownloaderOf(
                addDownloaderInUiProps.credentials
            ) ?: error("Downloader for ${addDownloaderInUiProps.credentials::class.qualifiedName} not found")
            val categoryItem by lazy {
                item.categoryId?.let {
                    runCatching {
                        downloadSystem.categoryManager.getCategoryById(it)
                    }.getOrNull()
                }
            }
            downloaderInUi.createBareDownloadItem(
                addDownloaderInUiProps.credentials,
                basicDownloadItem = BasicDownloadItem(
                    folder = item.folder
                        ?: categoryItem?.path
                        ?: appSettings.saveLocation.value,
                    name = item.name ?: addDownloaderInUiProps.extraConfig.suggestedName
                    ?: item.downloadSource.link.substringAfterLast("/"),
                ),
            )
        }

        val ids = downloadSystem.addDownload(
            newItemsToAdd = items.map {
                NewDownloadItemProps(
                    downloadItem = it,
                    onDuplicateStrategy = OnDuplicateStrategy.default(),
                    extraConfig = null,
                    context = EmptyContext,
                )
            },
        )
        for ((index, id) in ids.withIndex()) {
            val addedItem = request.items[index]
            addedItem.categoryId?.let {
                downloadSystem.categoryManager.addItemsToCategory(
                    it, listOf(id)
                )
            }
            addedItem.queueId?.let {
                downloadSystem.queueManager.addToQueue(
                    it, listOf(id)
                )
            }
        }
        if (request.options.silentStart) {
            ids.forEach {
                downloadSystem.userManualResume(it)
            }
        }
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
        return downloadSystem.downloadMonitor.downloadListFlow.value.filter {
            it.id in ids
        }.map {
            ShowDownloadIPC(
                name = it.name,
                folder = it.folder,
                id = it.id,
                status = ShowDownloadIPC.DownloadStatus.fromDownloadStatus(
                    it.statusOrFinished()
                ),
                percent = when (it) {
                    is CompletedDownloadItemState -> 100
                    is ProcessingDownloadItemState -> it.percent
                }
            )
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
