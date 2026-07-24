package com.abdownloadmanager.integration

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

sealed interface IntegrationResult {
    data object Inactive : IntegrationResult
    data class Fail(val throwable: Throwable) : IntegrationResult
    data class Success(val port: Int) : IntegrationResult
}

class Integration(
    val integrationHandler: IntegrationHandler,
    val scope: CoroutineScope,
    private val json: Json,
    val debugMode: Boolean,
) {

    private val portFlow = MutableStateFlow<IntegrationSettings?>(null)
    val integrationStatus = MutableStateFlow<IntegrationResult>(IntegrationResult.Inactive)

    fun enable(settings: IntegrationSettings) {
        portFlow.update { settings }
    }

    fun disable() {
        portFlow.update { null }
    }

    fun boot() {
        scope.launch {
            kotlin.runCatching {
                portFlow.collect { settings ->
                    runCatching {
                        if (settings != null) {
                            startServer(settings)
                            integrationStatus.update { IntegrationResult.Success(settings.port) }
                        } else {
                            stopServer()
                            integrationStatus.update { IntegrationResult.Inactive }
                        }
                    }.onFailure { throwable ->
                        integrationStatus.update {
                            IntegrationResult.Fail(throwable)
                        }
                        kotlin.runCatching {
                            disable()
                        }
                    }
                }
            }
        }
    }

    @Volatile
    private var server: MyServer? = null
    private suspend fun startServer(settings: IntegrationSettings) {
        stopServer()
        val server = createServer(settings)
        this.server = server
        withContext(Dispatchers.IO) {
//            println("start server")
            server.startMyServer()
        }
    }

    private suspend fun stopServer() {
        server?.let {
//            println("stop server")
            withContext(Dispatchers.IO) {
                it.stopMyServer()
            }
        }
        server = null
    }


    private fun createServer(settings: IntegrationSettings): MyServer {
        val server = embeddedServer(CIO, settings.port) {
            setupRouting(json, integrationHandler, settings)
        }
        return KtorServer(server)
    }
}
