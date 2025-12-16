package ir.amirab.downloader

import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem

class DownloaderRegistry {
    private val list = mutableSetOf<Downloader<IDownloadItem, DownloadJob, IDownloadCredentials>>()
    fun add(downloader: Downloader<*, *, *>) {
        @Suppress("UNCHECKED_CAST")
        list.add(downloader as Downloader<IDownloadItem, DownloadJob, IDownloadCredentials>)
    }

    fun remove(downloader: Downloader<*, *, *>) {
        list.remove(downloader)
    }

    fun createJob(
        downloadItem: IDownloadItem,
        downloadManager: DownloadManager,
    ): DownloadJob {
        val downloader = requireNotNull(
            list.firstOrNull {
                it.accept(downloadItem)
            }
        ) {
            "Download item '${downloadItem::class.qualifiedName}' not supported!"
        }
        return downloader.createJob(downloadItem, downloadManager)
    }

    fun getAll() = list.toList()
}
