package com.abdownloadmanager.desktop.nativemessaging.host

import com.abdownloadmanager.desktop.nativemessaging.host.stdio.NativeMessagingFinished
import com.abdownloadmanager.desktop.nativemessaging.host.stdio.TypeSafeStdioProtocol
import com.abdownloadmanager.desktop.utils.EntryType
import com.abdownloadmanager.desktop.utils.EntrypointInitializer
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import ir.amirab.util.logger.appLogger
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
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
        val result = try {
            logger.d { "Starting native messaging host..." }
            // Initialize Koin DI
            runBlocking {
                runMessageLoop()
            }
            logger.d { "Message loop ended normally" }
            0
        } catch (e: Exception) {
            if (e is NativeMessagingFinished) {
                logger.d { "Browser closed the input, exiting" }
                0
            } else {
                logger.e(e) { "Native messaging host can't finished properly: ${e.message}" }
                e.printStackTrace(System.err)
                1
            }
        }
        exitProcess(result)
    }

    /**
     * Main message loop - reads from stdin, processes, writes to stdout.
     */
    private suspend fun runMessageLoop() {

        while (true) {
            val request = try {
                channel.receive()
            } catch (e: Exception) {
                if (e is NativeMessagingFinished) {
                    throw e
                }
                appLogger.e(e) {
                    "channel.receive() failed with error"
                }
                replyError(e)
                if (e is SerializationException || e is IllegalArgumentException) {
                    //
                    continue
                } else {
                    // EOF / disconnected
                    throw e
                }
            }
            val response = try {
                router.handle(request)
            } catch (e: Exception) {
                appLogger.e(e) {
                    "router.handle(request) failed"
                }
                replyError(e)
                continue
            }

            try {
                channel.send(response)
            } catch (e: Exception) {
                appLogger.e(e) {
                    "channel.send(response) failed"
                }
                replyError(e)
            }
        }
    }

    private fun replyError(e: Exception) {
        try {
            val content = context(json) {
                NativeMessagingMessage(
                    id = NativeMessagingMessage.generateId(),
                    content = NativeMessagingMessage.Content.error(
                        message = e.localizedMessage
                    )
                )
            }
            channel.send(content)
        } catch (exception: Exception) {
            appLogger.e(exception) {
                "can't send the error to the browser"
            }
        }
    }
}

class NativeMessagingHostLauncher {
    fun main(args: Array<String>) {
        val json = Json { ignoreUnknownKeys = true }
        NativeMessagingHost(
            json = json,
            router = NativeMessagingRouter(
                json,
                DefinedNativeMessagingHandlers.getAll(
                    json, SingleInstanceManager.get().awokenAppIPCService()
                )
            )
        ).main(args)
    }
}

fun main(args: Array<String>) {
    runBlocking {
        EntrypointInitializer.boot(
            entryType = EntryType.NativeMessaging,
            debug = true,
        )
        NativeMessagingHostLauncher().main(args)
    }
}
