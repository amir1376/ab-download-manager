package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(
        list: List<DownloadCredentialsFromIntegration>,
        options: AddDownloadOptionsFromIntegration,
    )
    fun listQueues(): List<ApiQueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask)
}
