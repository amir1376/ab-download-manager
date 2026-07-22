package com.abdownloadmanager.desktop.cli.download.add.http

import com.abdownloadmanager.desktop.cli.download.add.shared.BaseNewDownload
import com.abdownloadmanager.integration.model.HttpDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.model.IDownloadCredentialsFromIntegration

class NewHttpDownload : BaseNewDownload("http") {
    val link by linkOption()
    val headers by headersOption()

    override fun createDownload(): IDownloadCredentialsFromIntegration {
        return HttpDownloadCredentialsFromIntegration(
            link = link,
            headers = headers.toMap(),
            downloadPage = downloadPage,
        )
    }
}
