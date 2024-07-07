package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(
        list: List<NewDownloadInfoFromIntegration>
    )
}