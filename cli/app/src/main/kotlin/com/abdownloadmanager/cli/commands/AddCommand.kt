package com.abdownloadmanager.cli.commands

import com.abdownloadmanager.cli.CliContext
import com.abdownloadmanager.integration.client.DesktopClient
import com.abdownloadmanager.integration.client.DesktopResult
import com.abdownloadmanager.integration.client.PortResolver
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import java.net.URI

class AddCommand : CliktCommand(
    name = "add",
    help = "Add a new download"
) {
    private val urls: List<String> by argument(
        help = "URL(s) to download"
    ).multiple()

    private val outputDir: String? by option(
        "--output-dir", "-o",
        help = "Output directory"
    )

    private val fileName: String? by option(
        "--name", "-n",
        help = "Output file name"
    )

    private val queue: Long? by option(
        "--queue", "-q",
        help = "Queue ID to add to"
    ).long()

    private val quiet: Boolean by option(
        "--quiet",
        help = "Suppress output messages"
    ).flag()

    private val username: String? by option(
        "--username", "-u",
        help = "HTTP Basic Auth username"
    )

    private val password: String? by option(
        "--password", "-p",
        help = "HTTP Basic Auth password"
    )

    override fun run() {
        val term = Terminal()
        val json = Json { ignoreUnknownKeys = true }

        // Ensure desktop app is running
        if (!CliContext.desktopLauncher.ensureDesktopRunning()) {
            term.println((TextColors.red)("Error: AB Download Manager is not available."))
            return
        }

        val client = DesktopClient(PortResolver.readIntegrationPort()
            ?: error("Integration port not configured"))
        var successCount = 0

        if (urls.isEmpty()) {
            term.println((TextColors.red)("Error: missing URL argument"))
            return
        }

        for (url in urls) {
            val itemName = fileName ?: url.substringAfterLast("/").substringBefore("?").ifEmpty {
                "download_${System.currentTimeMillis()}"
            }

            // Validate URL scheme — core only accepts http/https
            val urlScheme = runCatching { URI(url).scheme?.lowercase() }.getOrDefault(null)
            if (urlScheme == null || urlScheme !in listOf("http", "https")) {
                if (!quiet) {
                    term.println((TextColors.yellow)("! Skipping $url: unsupported scheme '$urlScheme'"))
                }
                continue
            }

            when (val result = client.addDownload(
                url = url,
                name = fileName,
                folder = outputDir,
                username = username,
                password = password,
                queueId = queue,
            )) {
                is DesktopResult.Success -> {
                    // Parse {"id":42} from response
                    val id = try {
                        json.decodeFromString<kotlinx.serialization.json.JsonObject>(result.data)["id"]?.jsonPrimitive?.long
                    } catch (_: Exception) {
                        null
                    }
                    successCount++
                    if (!quiet) {
                        val msg = if (id != null) {
                            "Download #$id: $itemName"
                        } else {
                            "Added: $itemName"
                        }
                        term.println((TextColors.green)("✓") + " $msg")
                    }
                }
                is DesktopResult.ConnectionError -> {
                    if (!quiet) {
                        term.println((TextColors.red)("✗") + " $url: ${result.message}")
                    }
                }
                is DesktopResult.RemoteError -> {
                    if (!quiet) {
                        term.println((TextColors.red)("✗") + " $url: server error (${result.statusCode}): ${result.body}")
                    }
                }
            }
        }

        if (!quiet && successCount > 0) {
            term.println((TextColors.green)("Successfully added $successCount download(s)"))
        }
    }
}