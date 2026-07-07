package com.abdownloadmanager.desktop.utils.singleInstance.service

import kotlinx.rpc.annotations.Rpc

@Rpc
interface ISingleInstanceService {
    suspend fun getIntegrationPort(): Int?
    suspend fun isReady(): Boolean
    suspend fun showUserThatAppIsRunning()
    suspend fun exit()
}
