package com.abdownloadmanager.integration

interface IntegrationHandler {
    suspend fun addDownload(list: List<NewDownloadInfoFromIntegration>)
    fun listQueues(): List<QueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask)
}
