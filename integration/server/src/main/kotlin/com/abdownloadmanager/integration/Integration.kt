package com.abdownloadmanager.integration

import com.abdownloadmanager.integration.http4k.MyHttp4KServer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

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


    private fun createServer(port: Int): MyServer {
        val handlers = HandlerMap().apply {
            post("/add") {
                runBlocking {
                    val itemsToAdd = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        AddDownloadsFromIntegration.createFromRequest(
                            json = json,
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
                    val jsonResponse = json.encodeToString(ListSerializer(ApiQueueModel.serializer()), queues)
                    MyResponse.Text(jsonResponse)
                }
            }
            post("/start-headless-download") {
                runBlocking {
                    val itemsToAdd = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        json.decodeFromString<NewDownloadTask>(message)
                    }
                    itemsToAdd.onFailure { it.printStackTrace() }
                    val id = integrationHandler.addDownloadTask(itemsToAdd.getOrThrow())
                    MyResponse.Json("""{"id":$id}""")
                }
            }
            post("/ping") {
                MyResponse.Text("pong")
            }
            get("/list") {
                runBlocking {
                    val downloads = integrationHandler.listDownloads()
                    val jsonResponse = json.encodeToString(ListSerializer(ApiDownloadModel.serializer()), downloads)
                    MyResponse.Text(jsonResponse)
                }
            }
            post("/info") {
                runBlocking {
                    val request = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        json.decodeFromString<IdRequest>(message)
                    }
                    request.onFailure { it.printStackTrace() }
                    val download = integrationHandler.getDownloadInfo(request.getOrThrow().id)
                    if (download != null) {
                        val jsonResponse = json.encodeToString(ApiDownloadModel.serializer(), download)
                        MyResponse.Text(jsonResponse)
                    } else {
                        MyResponse.Text("null")
                    }
                }
            }
            post("/pause") {
                runBlocking {
                    val request = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        json.decodeFromString<IdsRequest>(message)
                    }
                    request.onFailure { it.printStackTrace() }
                    integrationHandler.pauseDownloads(request.getOrThrow().ids)
                    MyResponse.Text("OK")
                }
            }
            post("/resume") {
                runBlocking {
                    val request = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        json.decodeFromString<IdsRequest>(message)
                    }
                    request.onFailure { it.printStackTrace() }
                    integrationHandler.resumeDownloads(request.getOrThrow().ids)
                    MyResponse.Text("OK")
                }
            }
            post("/remove") {
                runBlocking {
                    val request = kotlin.runCatching {
                        val message = it.getBody().orEmpty()
                        json.decodeFromString<RemoveRequest>(message)
                    }
                    request.onFailure { it.printStackTrace() }
                    integrationHandler.removeDownloads(request.getOrThrow().ids, request.getOrThrow().keepFile)
                    MyResponse.Text("OK")
                }
            }
        }
        return MyHttp4KServer(port, handlers, debugMode)
    }
}
