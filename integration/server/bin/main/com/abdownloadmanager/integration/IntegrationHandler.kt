package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(
        list: List<IDownloadCredentialsFromIntegration>,
        options: AddDownloadOptionsFromIntegration,
    )
    fun listQueues(): List<ApiQueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask)
}
