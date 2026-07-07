package com.abdownloadmanager.integration

import io.ktor.server.engine.EmbeddedServer

class KtorServer(
    private val server: EmbeddedServer<*, *>
) : MyServer {
    override fun stopMyServer() {
        server.stop()
    }

    override fun startMyServer() {
        server.start(wait = false)
    }
}
