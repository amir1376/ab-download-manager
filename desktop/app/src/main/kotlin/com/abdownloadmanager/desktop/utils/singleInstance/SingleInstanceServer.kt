package com.abdownloadmanager.desktop.utils.singleInstance

import ir.amirab.util.http4k.NanoHttp
import okio.*
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.server.asServer
import kotlin.concurrent.thread
import kotlin.io.path.exists
import kotlin.io.path.readText

class SingleInstanceServer(
    private val portPath: Path
) {
    private var serverInfo: ServerInfo? = null
    fun start(handle: HttpHandler) {
        val server = createServer(handle)
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
        serverInfo?.let {
            it.stop()
        }
    }

    private fun createServer(handle: HttpHandler): ServerInfo {
        val middlewares=ServerFilters.CatchAll.invoke{
            it.printStackTrace()
            ServerFilters.CatchAll.originalBehaviour(it)
        }
        val appRoutes = {req:Request->
//            println("new request $req")
            handle(req)
        }
        val server = middlewares
            .then(appRoutes)
            .asServer(NanoHttp("localhost",0))
        return ServerInfo(
            getPort = { server.port() },
            start = { server.start() },
            stop = { server.stop() },
        )
    }

    fun <T:Any>sendMessage(message: Command<T>): CommandResult<T> {
        val port = portPath
            .toNioPath()
            .takeIf { it.exists() }
            ?.runCatching { readText() }
            ?.getOrNull()
            ?.toIntOrNull()
                ?: return CommandResult.ServerNotExists()
        return typeSafeRequest(port, message)
    }
}


private data class ServerInfo(
    val getPort: ()->Int,
    val start: () -> Unit,
    val stop: () -> Unit,
)