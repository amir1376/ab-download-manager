package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(request: AddDownloadsFromIntegration)
    fun listQueues(): List<ApiQueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask)
}
