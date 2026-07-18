package com.abdownloadmanager.desktop.cli.download.add.http

import com.abdownloadmanager.desktop.cli.download.add.BaseNewDownload
import com.abdownloadmanager.integration.HttpDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
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
        return HttpDownloadCredentialsFromIntegration(
            link = url,
            headers = headers.toMap(),
            downloadPage = downloadPage,
        )
    }
}
