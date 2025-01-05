package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(
        list: List<NewDownloadInfoFromIntegration>
    )
    fun listQueues(): List<ApiQueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask)
}