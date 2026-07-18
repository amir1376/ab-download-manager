package com.abdownloadmanager.desktop.utils.native_messaging.host

import com.abdownloadmanager.desktop.utils.singleInstance.IPCServiceProvider
import com.abdownloadmanager.desktop.utils.singleInstance.service.AddDownloadFromIPC
import com.abdownloadmanager.desktop.utils.singleInstance.service.IDefaultAppIPCService
import com.abdownloadmanager.desktop.utils.singleInstance.withAwake
import com.abdownloadmanager.integration.AddDownloadsFromIntegration
import com.abdownloadmanager.integration.NewDownloadTask
import kotlinx.serialization.json.Json

class AddDownloadNativeRequestHandler(
    private val json: Json,
    private val appIPC: IPCServiceProvider<IDefaultAppIPCService>,
) : NativeMessagingMessageHandler {
    override fun accept(request: BrowserMessageRequest): Boolean {
        return request.action == "add"
    }

    override suspend fun handle(request: BrowserMessageRequest): NativeMessageResponse {
        val integrationRequest = json.decodeFromString<AddDownloadsFromIntegration>(request.payload)
        appIPC.withAwake().getService().useService {
            it.addDownload(
                AddDownloadFromIPC(
                    integrationRequest.items.map {
                        NewDownloadTask(it)
                    },
                    integrationRequest.options,
                )
            )
        }
        return NativeMessageResponse.Success.ok(request.requestId)
    }
}

class PingNativeRequestHandler : NativeMessagingMessageHandler {
    override fun accept(request: BrowserMessageRequest): Boolean {
        return request.action == "ping"
    }

    override suspend fun handle(request: BrowserMessageRequest): NativeMessageResponse {
        return NativeMessageResponse.Success(
            requestId = request.requestId,
            action = "Pong",
        )
    }
}

object DefinedNativeMessagingHandlers {
    fun getAll(
        json: Json, appIPC: IPCServiceProvider<IDefaultAppIPCService>,
    ): List<NativeMessagingMessageHandler> {
        return listOf(
            AddDownloadNativeRequestHandler(json, appIPC),
            PingNativeRequestHandler()
        )
    }
}
