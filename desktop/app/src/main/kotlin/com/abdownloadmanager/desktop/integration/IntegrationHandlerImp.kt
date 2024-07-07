package com.abdownloadmanager.desktop.integration

import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.desktop.AppComponent
import ir.amirab.downloader.downloaditem.DownloadCredentials
import com.abdownloadmanager.integration.NewDownloadInfoFromIntegration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IntegrationHandlerImp: IntegrationHandler,KoinComponent{
    val appComponent by inject<AppComponent>()
    override suspend fun addDownload(list: List<NewDownloadInfoFromIntegration>) {
        appComponent.openAddDownloadDialog(list.map {
            DownloadCredentials(
                link = it.link,
                headers = it.headers,
                downloadPage = it.downloadPage,
            )
        })
    }
}