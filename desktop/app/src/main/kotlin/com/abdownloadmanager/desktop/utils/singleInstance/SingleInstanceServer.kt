package com.abdownloadmanager.desktop.utils.singleInstance

import com.abdownloadmanager.desktop.utils.singleInstance.service.ISingleInstanceService
import io.ktor.client.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.withService
import okio.Path
import kotlin.concurrent.thread
import kotlin.io.path.exists
import kotlin.io.path.readText


class SingleInstanceServer(
    private val portPath: Path
) {
    private var serverInfo: ServerInfo? = null
    fun start() {
        val server = createServer()
        server.start()
        portPath.toFile().writeText(server.getPort().toString())
        Runtime
            .getRuntime()
            .addShutdownHook(thread(start = false) {
                stop()
            })

    }

    fun stop() {
        portPath.toFile().delete()
        serverInfo?.stop()
    }


    private fun createServer(): ServerInfo {
        val server = embeddedServer(CIO, port = 0) {
            setupKtorKRpcServer()
        }

        return ServerInfo(
            getPort = {
                runBlocking {
                    server.engine.resolvedConnectors().first().port
                }
            },
            start = { server.start(wait = false) },
            stop = { server.stop() },
        )
    }

    fun singleInstanceService(): CloseableService<ISingleInstanceService> {
        val port = portPath
            .toNioPath()
            .takeIf { it.exists() }
            ?.runCatching { readText() }
            ?.getOrNull()
            ?.toIntOrNull()
            ?: throw ServerNotExist()
        val ktorClient = getKtorClient()
        val kRpcClient = ktorClient.getRpcClient(port)
        val service = kRpcClient.withService<ISingleInstanceService>()
        return service.withCloseable(kRpcClient, ktorClient)
    }
}

private data class ServerInfo(
    val getPort: () -> Int,
    val start: () -> Unit,
    val stop: () -> Unit,
)

class ServerNotExist : Exception("Server Not Exists")

class CloseableService<T> internal constructor(
    val service: T,
    private val closeService: (T) -> Unit
) : AutoCloseable {
    override fun close() {
        closeService(service)
    }

    inline fun <R> useService(action: (T) -> R): R {
        return use {
            action(service)
        }
    }
}

private fun <T> T.withCloseable(
    kRpcClient: KrpcClient,
    client: HttpClient,
): CloseableService<T> {
    return CloseableService(
        service = this,
        closeService = {
            runCatching { kRpcClient.close() }
            runCatching { client.close() }
        }
    )
}
