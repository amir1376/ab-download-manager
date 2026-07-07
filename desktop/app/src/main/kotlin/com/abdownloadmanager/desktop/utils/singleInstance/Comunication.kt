package com.abdownloadmanager.desktop.utils.singleInstance

import com.abdownloadmanager.desktop.utils.singleInstance.service.SingleInstanceServiceImpl
import com.abdownloadmanager.desktop.utils.singleInstance.service.ISingleInstanceService
import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.serialization.json.Json

private val json by lazy {
    Json
}
private const val SINGLE_INSTANCE_RPC_PATH = "single-instance"

fun getKtorClient() = HttpClient(OkHttp) {
    installKrpc {
        serialization {
            json(json)
        }
    }
}

fun HttpClient.getRpcClient(port: Int): KrpcClient {
    return rpc {
        url("ws://localhost:$port/$SINGLE_INSTANCE_RPC_PATH")
    }
}

fun Application.setupKtorKRpcServer() {
    install(Krpc) {
        serialization {
            json(json)
        }
    }
    routing {
        rpc(SINGLE_INSTANCE_RPC_PATH) {
            registerService<ISingleInstanceService> {
                SingleInstanceServiceImpl()
            }
        }
    }
}



