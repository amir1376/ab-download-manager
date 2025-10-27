package com.abdownloadmanager.shared.downloaderinui

import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.DownloadItemStateFactory
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import kotlin.reflect.KClass

typealias TADownloaderInUI = DownloaderInUi<
        IDownloadCredentials,
        IResponseInfo,
        LinkChecker<IDownloadCredentials, IResponseInfo>,
        IDownloadItem,
        NewDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, LinkChecker<IDownloadCredentials, IResponseInfo>>,
        EditDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, LinkChecker<IDownloadCredentials, IResponseInfo>, CredentialAndItemMapper<IDownloadCredentials, IDownloadItem>>,
        CredentialAndItemMapper<IDownloadCredentials, IDownloadItem>,
        DownloadJob,
        Downloader<IDownloadItem, DownloadJob, IDownloadCredentials>>

class DownloaderInUiRegistry
    : DownloadItemStateFactory<IDownloadItem, DownloadJob> {
    private val list = mutableListOf<TADownloaderInUI>()
    private val componentHashes = hashMapOf<Any, TADownloaderInUI>()
    fun add(downloaderInUi: DownloaderInUi<*, *, *, *, *, *, *, *, *>) {
        // the compiler gave me error when I add these two generics (TDownloadJob, TDownloader) into the DownloaderInUi
        val element = downloaderInUi as TADownloaderInUI
        @Suppress("UNCHECKED_CAST")
        list.add(element)
        getComponentsOf(element).forEach {
            componentHashes[it] = element
        }
    }

    fun remove(downloaderInUi: DownloaderInUi<*, *, *, *, *, *, *, *, *>) {
        list.remove(downloaderInUi)
        @Suppress("UNCHECKED_CAST")
        getComponentsOf(
            downloaderInUi as TADownloaderInUI
        ).forEach {
            componentHashes.remove(it)
        }
    }

    fun getDownloaderOf(downloadItem: IDownloadCredentials): TADownloaderInUI? {
        componentHashes.get(downloadItem::class)?.let {
            // fast path without iterating the list
            return it
        }
        // IDownloadCredentials is an intermediate interface so we should iterate the list instead
        return list.firstOrNull {
            it.acceptDownloadCredentials(downloadItem)
        }
    }

    private fun getDownloaderOf(downloadJob: DownloadJob): TADownloaderInUI? {
        return getDownloaderOf(downloadJob.downloadItem)
    }

    fun bestMatchForThisLink(link: String): TADownloaderInUI? {
        return list.firstOrNull {
            it.supportsThisLink(link)
        }
    }

    fun getAll(): List<TADownloaderInUI> {
        return list.toList()
    }


    override fun createProcessingDownloadItemStateFromDownloadJob(
        downloadJob: DownloadJob,
        speed: Long
    ): ProcessingDownloadItemState {
        return requireNotNull(getDownloaderOf(downloadJob)) {
            "there is no downloader in UI registered for this download job: ${downloadJob::class.qualifiedName}"
        }.createProcessingDownloadItemState(downloadJob, speed)
    }

    override fun createCompletedDownloadItemStateFromDownloadItem(downloadItem: IDownloadItem): CompletedDownloadItemState {
        return requireNotNull(getDownloaderOf(downloadItem)) {
            "there is no downloader in UI registered for this download item: ${downloadItem::class.qualifiedName}"
        }.createCompletedDownloadItemState(downloadItem)
    }

    companion object {
        private fun getComponentsOf(element: TADownloaderInUI): List<KClass<out Any>> {
            return listOf(
                element.downloader.downloadJobClass,
                element.downloader.downloadItemClass,
            )
        }
    }

}
