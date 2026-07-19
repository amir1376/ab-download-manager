package com.abdownloadmanager.desktop.nativemessaging.host

import com.abdownloadmanager.desktop.utils.singleInstance.IPCServiceProviderAwakerSupport
import com.abdownloadmanager.desktop.utils.singleInstance.service.IDefaultAppIPCService
import com.abdownloadmanager.integration.model.AddDownloadsFromIntegration
import kotlinx.serialization.json.Json

class AddDownloadNativeRequestHandler(
    private val json: Json,
    private val appIPC: IPCServiceProviderAwakerSupport<IDefaultAppIPCService>,
) : NativeMessagingMessageHandler {
    override fun accept(request: NativeMessagingMessage.Content): Boolean {
        return request.action == "add"
    }

    override suspend fun handle(request: NativeMessagingMessage.Content): NativeMessagingMessage.Content {
        val integrationRequest = json.decodeFromString<AddDownloadsFromIntegration>(request.payload)
        appIPC.getService().useService {
            it.addDownloadByGui(integrationRequest)
        }
        return context(json) {
            NativeMessagingMessage.Content.boolean(true)
        }
    }
}

class PingNativeRequestHandler(
    private val json: Json
) : NativeMessagingMessageHandler {
    override fun accept(request: NativeMessagingMessage.Content): Boolean {
        return request.action == "ping"
    }

    override suspend fun handle(request: NativeMessagingMessage.Content): NativeMessagingMessage.Content {
        return context(json) {
            NativeMessagingMessage.Content.boolean(true)
        }
    }
}

object DefinedNativeMessagingHandlers {
    fun getAll(
        json: Json, appIPC: IPCServiceProviderAwakerSupport<IDefaultAppIPCService>,
    ): List<NativeMessagingMessageHandler> {
        return listOf(
            AddDownloadNativeRequestHandler(json, appIPC),
            PingNativeRequestHandler(json)
        )
    }
}
