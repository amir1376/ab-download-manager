package com.abdownloadmanager.desktop.utils.native_messaging.host

import com.abdownloadmanager.desktop.utils.singleInstance.IPCServiceProviderAwakerSupport
import com.abdownloadmanager.desktop.utils.singleInstance.service.IDefaultAppIPCService
import com.abdownloadmanager.integration.model.AddDownloadsFromIntegration
import kotlinx.serialization.json.Json

class AddDownloadNativeRequestHandler(
    private val json: Json,
    private val appIPC: IPCServiceProviderAwakerSupport<IDefaultAppIPCService>,
) : NativeMessagingMessageHandler {
    override fun accept(request: BrowserMessageRequest): Boolean {
        return request.action == "add"
    }

    override suspend fun handle(request: BrowserMessageRequest): NativeMessageResponse {
        val integrationRequest = json.decodeFromString<AddDownloadsFromIntegration>(request.payload)
        appIPC.getService().useService {
            it.addDownloadByGui(integrationRequest)
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
        json: Json, appIPC: IPCServiceProviderAwakerSupport<IDefaultAppIPCService>,
    ): List<NativeMessagingMessageHandler> {
        return listOf(
            AddDownloadNativeRequestHandler(json, appIPC),
            PingNativeRequestHandler()
        )
    }
}
