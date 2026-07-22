package com.abdownloadmanager.desktop.cli.download.add.hls

import com.abdownloadmanager.desktop.cli.download.add.shared.BaseNewDownload
import com.abdownloadmanager.integration.model.HLSDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.model.IDownloadCredentialsFromIntegration

class NewHlsDownload : BaseNewDownload("hls") {

    val link by linkOption()
    val headers by headersOption()


    override fun createDownload(): IDownloadCredentialsFromIntegration {
        return HLSDownloadCredentialsFromIntegration(
            link = link,
            headers = headers.toMap(),
            downloadPage = downloadPage,
        )
    }
}
