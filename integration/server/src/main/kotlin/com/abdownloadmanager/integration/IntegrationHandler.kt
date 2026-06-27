package com.abdownloadmanager.integration

interface IntegrationHandler{
    suspend fun addDownload(
        list: List<IDownloadCredentialsFromIntegration>,
        options: AddDownloadOptionsFromIntegration,
    )
    fun listQueues(): List<ApiQueueModel>
    suspend fun addDownloadTask(task: NewDownloadTask): Long

    suspend fun listDownloads(): List<ApiDownloadModel>
    suspend fun getDownloadInfo(id: Long): ApiDownloadModel?
    suspend fun pauseDownloads(ids: List<Long>)
    suspend fun resumeDownloads(ids: List<Long>)
    suspend fun removeDownloads(ids: List<Long>, keepFile: Boolean)
}
