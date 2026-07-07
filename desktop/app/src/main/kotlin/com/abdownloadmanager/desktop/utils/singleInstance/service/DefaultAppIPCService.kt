package com.abdownloadmanager.desktop.utils.singleInstance.service

import com.abdownloadmanager.integration.AddDownloadsFromIntegration
import com.abdownloadmanager.integration.ApiQueueModel
import kotlinx.rpc.annotations.Rpc

@Rpc
interface DefaultAppIPCService {
    suspend fun addDownload(request: AddDownloadsFromIntegration)
    suspend fun listQueues(): List<ApiQueueModel>
}
