package ir.amirab.downloader.downloaditem.ytdlp

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.Downloader
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class YtdlpDownloader(
    private val ytdlpExecutablePathProvider: () -> String,
) : Downloader<YtdlpDownloadItem, YtdlpDownloadJob, YtdlpDownloadCredentials> {
    override fun createJob(
        item: YtdlpDownloadItem,
        downloadManager: DownloadManager
    ): YtdlpDownloadJob {
        return YtdlpDownloadJob(item, downloadManager, ytdlpExecutablePathProvider)
    }

    override fun accept(item: IDownloadItem): Boolean {
        return item is YtdlpDownloadItem
    }

    override val downloadItemClass: KClass<YtdlpDownloadItem> = YtdlpDownloadItem::class
    override val downloadCredentialsClass: KClass<YtdlpDownloadCredentials> = YtdlpDownloadCredentials::class
    override val downloadJobClass: KClass<YtdlpDownloadJob> = YtdlpDownloadJob::class
    override val downloadItemSerializer: KSerializer<YtdlpDownloadItem> = YtdlpDownloadItem.serializer()
    override val downloadCredentialsSerializer: KSerializer<YtdlpDownloadCredentials> = YtdlpDownloadCredentials.serializer()
}
