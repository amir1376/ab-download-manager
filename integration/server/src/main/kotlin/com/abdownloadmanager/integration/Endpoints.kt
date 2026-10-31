package com.abdownloadmanager.integration

import com.abdownloadmanager.integration.model.AddDownloadsFromIntegration
import com.abdownloadmanager.integration.model.ApiQueueModel
import com.abdownloadmanager.integration.model.NewDownloadTask
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.apikey.apiKey
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private data class AppPrincipal(val key: String)

private const val AuthOptional = "auth-optional"
private const val AuthRequired = "auth-required"

internal fun Application.setupRouting(
    json: Json,
    integrationHandler: IntegrationHandler,
    settings: IntegrationSettings,
) {
    val apiKey = settings.apiKey
    install(Authentication) {
        apiKey(AuthOptional) {
            validate { receivedKey ->
                if (receivedKey == apiKey) {
                    AppPrincipal(receivedKey)
                } else null
            }
            skipWhen {
                !settings.apiKeyEnforced
            }
        }
        apiKey(AuthRequired) {
            validate { receivedKey ->
                if (receivedKey == apiKey) {
                    AppPrincipal(receivedKey)
                } else null
            }
        }
    }
    routing {
        authenticate(AuthOptional) {
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
                    integrationHandler.addDownloadByGui(
                        AddDownloadsFromIntegration(
                            newImportRequest.items,
                            newImportRequest.options,
                        )
                    )
                }
                call.respondText("OK")
            }
            get("/queues") {
                val queues = integrationHandler.listQueues()
                val jsonResponse = json.encodeToString(ListSerializer(ApiQueueModel.serializer()), queues)
                call.respondText(jsonResponse)
            }
            post("/ping") {
                call.respondText("pong")
            }
        }
        authenticate(AuthRequired) {
            post("/start-headless-download") {
                val itemsToAdd = kotlin.runCatching {
                    val message = call.receiveText()
                    json.decodeFromString<NewDownloadTask>(message)
                }
                itemsToAdd.onFailure { it.printStackTrace() }
                integrationHandler.addDownload(itemsToAdd.getOrThrow())
                call.respondText("OK")
            }
        }
    }
}
