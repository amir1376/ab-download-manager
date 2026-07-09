package com.abdownloadmanager.cli.commands

import com.abdownloadmanager.cli.CliContext
import com.abdownloadmanager.integration.client.DesktopClient
import com.abdownloadmanager.integration.client.DesktopResult
import com.abdownloadmanager.integration.client.PortResolver
import com.abdownloadmanager.integration.ApiDownloadModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.SizeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.builtins.ListSerializer

class ListCommand : CliktCommand(
    name = "list",
    help = "List all downloads"
) {
    private val all: Boolean by option("--all", "-a", help = "Show all downloads including finished").flag()
    private val json: Boolean by option("--json", help = "Output as JSON").flag()

    override fun run() {
        val term = Terminal()
        val jsonParser = Json { ignoreUnknownKeys = true; prettyPrint = true }

        // Ensure desktop app is running
        if (!CliContext.desktopLauncher.ensureDesktopRunning()) {
            term.println((TextColors.red)("Error: AB Download Manager is not available."))
            return
        }

        val client = DesktopClient(PortResolver.readIntegrationPort()
            ?: error("Integration port not configured"))

        when (val result = client.listDownloads()) {
            is DesktopResult.Success -> {
                val allItems = try {
                    jsonParser.decodeFromString(
                        ListSerializer(ApiDownloadModel.serializer()),
                        result.data
                    )
                } catch (e: Exception) {
                    term.println((TextColors.red)("Failed to parse server response: ${e.message}"))
                    return
                }

                val items = if (all) allItems else allItems.filter { it.status != "Completed" }

                if (items.isEmpty()) {
                    if (json) {
                        term.println("[]")
                    } else {
                        term.println((TextColors.yellow)("No downloads found."))
                    }
                    return
                }

                if (json) {
                    term.println(jsonParser.encodeToString(ListSerializer(ApiDownloadModel.serializer()), items))
                } else {
                    printTable(term, items)
                }
            }
            is DesktopResult.ConnectionError -> {
                term.println((TextColors.red)("Failed to connect to AB Download Manager: ${result.message}"))
            }
            is DesktopResult.RemoteError -> {
                term.println((TextColors.red)("Server error (${result.statusCode}): ${result.body}"))
            }
        }
    }

    private fun printTable(term: Terminal, items: List<ApiDownloadModel>) {
        term.println((TextColors.brightBlue)("┌──────┬──────────────────────────────────────┬──────────────┬──────────────┐"))
        term.println((TextColors.brightBlue)("│ ${"ID".padEnd(4)} │ ${"Name".padEnd(36)} │ ${"Status".padEnd(12)} │ ${"Size".padEnd(12)} │"))
        term.println((TextColors.brightBlue)("├──────┼──────────────────────────────────────┼──────────────┼──────────────┤"))

        for (item in items) {
            val displayName = item.name.take(36)
            val statusText = when (item.status) {
                "Completed" -> "Completed"
                "Paused" -> "Paused"
                "Downloading" -> "Downloading"
                "Error" -> "Error"
                "Added" -> "Queued"
                else -> item.status
            }
            val paddedStatus = statusText.padEnd(12)
            val displayStatus = when (item.status) {
                "Completed" -> (TextColors.green)(paddedStatus)
                "Error" -> (TextColors.red)(paddedStatus)
                else -> paddedStatus
            }
            val size = if (item.size > 0) {
                SizeConverter.bytesToSize(item.size, CommonSizeConvertConfigs.BinaryBytes).toString()
            } else {
                "?"
            }

            term.print((TextColors.brightBlue)("│ "))
            term.print(item.id.toString().padEnd(4))
            term.print((TextColors.brightBlue)(" │ "))
            term.print(displayName.padEnd(36))
            term.print((TextColors.brightBlue)(" │ "))
            term.print(displayStatus)
            term.print((TextColors.brightBlue)(" │ "))
            term.print(size.padEnd(12))
            term.println((TextColors.brightBlue)(" │"))
        }

        term.println((TextColors.brightBlue)("└──────┴──────────────────────────────────────┴──────────────┴──────────────┘"))
        term.println("Total: ${items.size} download(s)")
    }
}