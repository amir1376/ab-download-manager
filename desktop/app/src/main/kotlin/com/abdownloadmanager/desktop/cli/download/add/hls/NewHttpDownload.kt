package com.abdownloadmanager.desktop.cli.download.add.hls

import com.abdownloadmanager.desktop.cli.download.add.BaseNewDownload
import com.abdownloadmanager.integration.HLSDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.github.ajalt.clikt.parameters.options.*
import ir.amirab.util.HttpUrlUtils

class NewHttpDownload : BaseNewDownload("http") {
    val url by option("--url")
        .required()
        .validate { HttpUrlUtils.isValidUrl(it) }

    val headers by option("header")
        .convert {
            val (key, value) = it.split('=', ':')
            key to value
        }
        .multiple()


    override fun createDownload(): IDownloadCredentialsFromIntegration {
        return HLSDownloadCredentialsFromIntegration(
            link = url,
            headers = headers.toMap(),
            downloadPage = downloadPage,
        )
    }
}
