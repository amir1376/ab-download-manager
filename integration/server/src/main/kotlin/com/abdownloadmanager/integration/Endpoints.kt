package com.abdownloadmanager.integration

import io.ktor.server.application.Application
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal fun Application.setupRouting(
    json: Json, integrationHandler: IntegrationHandler,
) {
    routing {
        post("/add") {
            val itemsToAdd = kotlin.runCatching {
                val message = call.receiveText()
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
            call.respondText("OK")
        }
        get("/queues") {
            val queues = integrationHandler.listQueues()
            val jsonResponse = json.encodeToString(ListSerializer(ApiQueueModel.serializer()), queues)
            call.respondText(jsonResponse)
        }
        post("/start-headless-download") {
            val itemsToAdd = kotlin.runCatching {
                val message = call.receiveText()
                json.decodeFromString<NewDownloadTask>(message)
            }
            itemsToAdd.onFailure { it.printStackTrace() }
            integrationHandler.addDownloadTask(itemsToAdd.getOrThrow())
            call.respondText("OK")
        }
        post("/ping") {
            call.respondText("pong")
        }
    }
}
