package com.abdownloadmanager.shared.util.extractors.linkextractor

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DownloadCredentialsFromSimpleLink :
    DownloadCredentialExtractor<String>, KoinComponent {
    val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    override fun extract(input: String): List<IDownloadCredentials> {
        return StringUrlExtractor.extract(input)
            .mapNotNull {
                downloaderInUiRegistry
                    .bestMatchForThisLink(it)
                    ?.createMinimumCredentials(it)
            }
    }
}
