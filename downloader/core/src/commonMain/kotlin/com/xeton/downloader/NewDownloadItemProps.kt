package com.xeton.downloader

import com.xeton.downloader.downloaditem.DownloadItemContext
import com.xeton.downloader.downloaditem.DownloadJobExtraConfig
import com.xeton.downloader.downloaditem.IDownloadItem
import com.xeton.downloader.utils.OnDuplicateStrategy

data class NewDownloadItemProps(
    val downloadItem: IDownloadItem,
    val extraConfig: DownloadJobExtraConfig?,
    val onDuplicateStrategy: OnDuplicateStrategy,
    val context: DownloadItemContext,
)
