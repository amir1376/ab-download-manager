package com.abdownloadmanager.desktop.utils.native_messaging.host

import com.abdownloadmanager.desktop.storage.AppSettingsModel
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.native_messaging.host.stdio.TypeSafeStdioProtocol
import com.abdownloadmanager.desktop.utils.singleInstance.IPCServiceProvider
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import ir.amirab.util.logger.appLogger
import ir.amirab.util.readText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import java.io.EOFException
import java.io.IOException
import kotlin.system.exitProcess


/**
 * Native Messaging Host for browser extension communication.
 */
class NativeMessagingHost(
    private val json: Json,
    private val router: NativeMessagingRouter,
) : KoinComponent {

    private val logger = appLogger.withTag("NativeMessagingHost")

    private val channel by lazy {
        val input = System.`in`
        val output = System.out
        TypeSafeStdioProtocol(
            json,
            input,
            output,
        )
    }

    fun main(args: Array<String>) {
        try {
            logger.d { "Starting native messaging host..." }
            // Initialize Koin DI
            runBlocking {
                runMessageLoop()
            }
            logger.d { "Message loop ended normally" }
            exitProcess(0)
        } catch (e: Exception) {
            logger.d { "Fatal error: ${e.message}" }
            e.printStackTrace(System.err)
            exitProcess(1)
        }
    }

    /**
     * Main message loop - reads from stdin, processes, writes to stdout.
     */
    private suspend fun runMessageLoop() {

        while (true) {
            val request = try {
                channel.receive()
            } catch (e: IOException) {
                // EOF reached - browser disconnected
                null
            } catch (e: Exception) {
                val content = NativeMessageResponse.Error(
                    requestId = -1,
                    message = e.localizedMessage
                )
                channel.send(content)
                null
            } ?: break
            try {
                val response = router.handle(request)
                channel.send(response)
            } catch (e: EOFException) {
                break
            } catch (e: Exception) {
                try {
                    val content = NativeMessageResponse.Error(
                        requestId = request.requestId,
                        message = e.localizedMessage
                    )
                    channel.send(content)
                } catch (writeError: Exception) {
                    break
                }
            }
        }
    }
}

object NativeMessagingHostLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val json = Json { ignoreUnknownKeys = true }
        val port =
            runCatching { json.decodeFromString<AppSettingsModel>(AppInfo.definedPaths.appSettingsFile.readText()) }
                .getOrElse { AppSettingsModel.default }
                .let { s ->
                    s.browserIntegrationPort.takeIf { s.browserIntegrationEnabled }
                }
                ?: run {
                    // user explicitly disabled the integration
                    exitProcess(1)
                }

        NativeMessagingHost(
            json = json,
            router = NativeMessagingRouter(
                DefinedNativeMessagingHandlers.getAll(
                    json, IPCServiceProvider.from {
                        SingleInstanceManager.get().appIPCService()
                    }
                )
            )
        ).main(args)
    }
}
