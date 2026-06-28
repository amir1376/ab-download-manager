package com.abdownloadmanager.shared.downloaderinui

import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.xeton.downloader.Downloader
import com.xeton.downloader.connection.IResponseInfo
import com.xeton.downloader.downloaditem.DownloadJob
import com.xeton.downloader.downloaditem.IDownloadCredentials
import com.xeton.downloader.downloaditem.IDownloadItem
import com.xeton.downloader.monitor.CompletedDownloadItemState
import com.xeton.downloader.monitor.DownloadItemStateFactory
import com.xeton.downloader.monitor.ProcessingDownloadItemFactoryInputs
import com.xeton.downloader.monitor.ProcessingDownloadItemState
import kotlin.reflect.KClass

typealias TADownloaderInUI = DownloaderInUi<
        IDownloadCredentials,
        IResponseInfo,
        DownloadSize,
        LinkChecker<IDownloadCredentials, IResponseInfo, DownloadSize>,
        IDownloadItem,
        NewDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, DownloadSize, LinkChecker<IDownloadCredentials, IResponseInfo, DownloadSize>>,
        EditDownloadInputs<IDownloadItem, IDownloadCredentials, IResponseInfo, DownloadSize, LinkChecker<IDownloadCredentials, IResponseInfo, DownloadSize>, CredentialAndItemMapper<IDownloadCredentials, IDownloadItem>>,
        CredentialAndItemMapper<IDownloadCredentials, IDownloadItem>,
        DownloadJob,
        Downloader<IDownloadItem, DownloadJob, IDownloadCredentials>>

class DownloaderInUiRegistry
    : DownloadItemStateFactory<IDownloadItem, DownloadJob> {
    private val list = mutableListOf<TADownloaderInUI>()
    private val componentHashes = hashMapOf<Any, TADownloaderInUI>()
    fun add(downloaderInUi: DownloaderInUi<*, *, *, *, *, *, *, *, *, *>) {
        // the compiler gave me error when I add these two generics (TDownloadJob, TDownloader) into the DownloaderInUi
        val element = downloaderInUi as TADownloaderInUI
        @Suppress("UNCHECKED_CAST")
        list.add(element)
        getComponentsOf(element).forEach {
            componentHashes[it] = element
        }
    }

    fun remove(downloaderInUi: DownloaderInUi<*, *, *, *, *, *, *, *, *, *>) {
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


    override fun createProcessingDownloadItemState(
        props: ProcessingDownloadItemFactoryInputs<DownloadJob>,
    ): ProcessingDownloadItemState {
        val downloadJob = props.downloadJob
        return requireNotNull(getDownloaderOf(downloadJob)) {
            "there is no downloader in UI registered for this download job: ${downloadJob::class.qualifiedName}"
        }.createProcessingDownloadItemState(props)
    }

    override fun createCompletedDownloadItemState(downloadItem: IDownloadItem): CompletedDownloadItemState {
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
