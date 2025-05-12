package com.abdownloadmanager.integration

import com.abdownloadmanager.integration.http4k.MyHttp4KServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

//val scope = CoroutineScope(SupervisorJob())

/*
fun main() {
    runBlocking {
        val integration = Integration(
            object : ir.amirab.abdownloadmanager.integration.IntegrationHandler {
                override suspend fun addDownload(list: List<NewDownloadInfo>){
                    return
                }
            },
            this,
        )
//    integration.createServer(8000)
//        .start(true)
        integration.boot()
        integration.enable(8000)
        delay(100)
    }
}
*/
sealed interface IntegrationResult {
    data object Inactive : IntegrationResult
    data class Fail(val throwable: Throwable) : IntegrationResult
    data class Success(val port: Int) : IntegrationResult
}

class Integration(
    val integrationHandler: IntegrationHandler,
    val scope: CoroutineScope,
    val debugMode: Boolean,
) {

    private val portFlow = MutableStateFlow<Int?>(null)
    val integrationStatus = MutableStateFlow<IntegrationResult>(IntegrationResult.Inactive)

    fun enable(port: Int) {
        portFlow.update { port }
    }

    fun disable() {
        portFlow.update { null }
    }

    fun boot() {
        scope.launch {
            kotlin.runCatching {
                portFlow.collect { port ->
                    runCatching {
                        if (port != null) {
                            startServer(port)
                            integrationStatus.update { IntegrationResult.Success(port) }
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
    private suspend fun startServer(port: Int) {
        stopServer()
        val server = createServer(port)
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

    private val customJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun createServer(port: Int): MyServer {
        val handlers = HandlerMap().apply {
            post("/add") {
                runBlocking {
                    val itemsToAdd = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        AddDownloadsFromIntegration.createFromRequest(
                            json = customJson,
                            jsonData = message
                        )
                    }
                    itemsToAdd.onFailure { it.printStackTrace() }
                    itemsToAdd.getOrThrow().let { newImportRequest ->
                        integrationHandler.addDownload(
                            newImportRequest.items,
                            newImportRequest.options,
                        )
                    }
                }
                MyResponse.Text("OK")
            }
            get("/queues") {
                runBlocking {
                    val queues = integrationHandler.listQueues()
                    val jsonResponse = customJson.encodeToString(ListSerializer(ApiQueueModel.serializer()), queues)
                    MyResponse.Text(jsonResponse)
                }
            }
            post("/start-headless-download") {
                runBlocking {
                    val itemsToAdd = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        customJson.decodeFromString<NewDownloadTask>(message)
                    }
                    itemsToAdd.onFailure { it.printStackTrace() }
                    integrationHandler.addDownloadTask(itemsToAdd.getOrThrow())
                }
                MyResponse.Text("OK")
            }
            post("/ping") {
                MyResponse.Text("pong")
            }
        }
        return MyHttp4KServer(port, handlers, debugMode)
    }
}
