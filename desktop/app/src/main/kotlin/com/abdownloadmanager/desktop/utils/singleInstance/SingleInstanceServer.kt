package com.abdownloadmanager.desktop.utils.singleInstance

import com.abdownloadmanager.desktop.utils.singleInstance.service.IDefaultAppIPCService
import com.abdownloadmanager.desktop.utils.singleInstance.service.ISingleInstanceService
import io.ktor.client.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.rpc.annotations.Rpc
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

    fun singleInstanceService() = getIPCService<ISingleInstanceService>()
    fun appIPCService() = getIPCService<IDefaultAppIPCService>()

    private inline fun <@Rpc reified T : Any> getIPCService(): CloseableService<T> {
        val port = portPath
            .toNioPath()
            .takeIf { it.exists() }
            ?.runCatching { readText() }
            ?.getOrNull()
            ?.toIntOrNull()
            ?: throw ServerNotExist()
        val ktorClient = getKtorClient()
        val kRpcClient = ktorClient.getRpcClient(port)
        val service = kRpcClient.withService<T>()
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

interface IPCServiceProvider<T> {
    fun getService(): CloseableService<T>

    companion object {
        fun <T> from(provider: () -> CloseableService<T>): IPCServiceProvider<T> {
            return object : IPCServiceProvider<T> {
                override fun getService(): CloseableService<T> {
                    return provider()
                }
            }
        }
    }
}

class IPCServiceProviderAwakerSupport<T>(
    private val serviceProvider: IPCServiceProvider<T>,
) {
    // awake the app if it's not running
    suspend fun getService(): CloseableService<T> {
        withContext(Dispatchers.IO) {
            SingleInstanceManager.get().awakeMainInstance()
        }
        return serviceProvider.getService()
    }
}

fun <T> IPCServiceProvider<T>.withAwake(): IPCServiceProviderAwakerSupport<T> {
    return IPCServiceProviderAwakerSupport(this)
}
