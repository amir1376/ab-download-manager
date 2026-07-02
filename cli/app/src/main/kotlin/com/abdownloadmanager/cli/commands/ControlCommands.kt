package com.abdownloadmanager.cli.commands

import com.abdownloadmanager.cli.client.DesktopLauncher
import com.abdownloadmanager.cli.client.DesktopClient
import com.abdownloadmanager.cli.client.DesktopResult
import com.abdownloadmanager.cli.utils.CliFormatting
import com.abdownloadmanager.cli.utils.PortResolver
import com.abdownloadmanager.integration.ApiDownloadModel
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.builtins.ListSerializer

/**
 * Base class for control commands (pause/resume/remove) that share the
 * desktop app detection pattern.
 */
abstract class BaseControlCommand(
    name: String,
    help: String,
) : CliktCommand(name = name, help = help) {
    protected val ids: List<Long> by argument(
        help = "Download ID(s)"
    ).long().multiple()

    final override fun run() {
        val term = Terminal()

        if (!DesktopLauncher.ensureDesktopRunning()) {
            term.println((TextColors.red)("Error: AB Download Manager is not available."))
            return
        }

        if (ids.isEmpty()) {
            term.println((TextColors.red)("Error: missing download ID(s)"))
            return
        }

        val client = DesktopClient(PortResolver.readIntegrationPort()
            ?: error("Integration port not configured"))
        runWithClient(term, client)
    }

    protected abstract fun runWithClient(term: Terminal, client: DesktopClient)
}

class PauseCommand : BaseControlCommand(
    name = "pause",
    help = "Pause one or more downloads"
) {
    override fun runWithClient(term: Terminal, client: DesktopClient) {
        when (val result = client.pauseDownloads(ids)) {
            is DesktopResult.Success -> {
                term.println((TextColors.green)("Paused ${ids.size} download(s)"))
            }
            is DesktopResult.ConnectionError -> {
                term.println((TextColors.red)("Connection error: ${result.message}"))
            }
            is DesktopResult.RemoteError -> {
                term.println((TextColors.red)("Server error (${result.statusCode}): ${result.body}"))
            }
        }
    }
}

class ResumeCommand : BaseControlCommand(
    name = "resume",
    help = "Resume one or more downloads"
) {
    override fun runWithClient(term: Terminal, client: DesktopClient) {
        when (val result = client.resumeDownloads(ids)) {
            is DesktopResult.Success -> {
                term.println((TextColors.green)("Resumed ${ids.size} download(s)"))
            }
            is DesktopResult.ConnectionError -> {
                term.println((TextColors.red)("Connection error: ${result.message}"))
            }
            is DesktopResult.RemoteError -> {
                term.println((TextColors.red)("Server error (${result.statusCode}): ${result.body}"))
            }
        }
    }
}

class RemoveCommand : BaseControlCommand(
    name = "remove",
    help = "Remove one or more downloads"
) {
    private val keepFile: Boolean by option("--keep-file", "-k", help = "Keep the downloaded file").flag(default = true)

    override fun runWithClient(term: Terminal, client: DesktopClient) {
        when (val result = client.removeDownloads(ids, keepFile)) {
            is DesktopResult.Success -> {
                term.println((TextColors.green)("Removed ${ids.size} download(s)"))
            }
            is DesktopResult.ConnectionError -> {
                term.println((TextColors.red)("Connection error: ${result.message}"))
            }
            is DesktopResult.RemoteError -> {
                term.println((TextColors.red)("Server error (${result.statusCode}): ${result.body}"))
            }
        }
    }
}

class InfoCommand : CliktCommand(
    name = "info",
    help = "Show detailed information about a download"
) {
    private val ids: List<Long> by argument(
        help = "Download ID(s)"
    ).long().multiple()

    private val jsonOutput: Boolean by option("--json", help = "Output as JSON").flag()

    override fun run() {
        val term = Terminal()
        val jsonParser = Json { ignoreUnknownKeys = true; prettyPrint = true }

        if (!DesktopLauncher.ensureDesktopRunning()) {
            term.println((TextColors.red)("Error: AB Download Manager is not available."))
            return
        }

        if (ids.isEmpty()) {
            term.println((TextColors.red)("Error: missing download ID(s)"))
            return
        }

        val client = DesktopClient(PortResolver.readIntegrationPort()
            ?: error("Integration port not configured"))
        val foundItems = mutableListOf<ApiDownloadModel>()

        for (id in ids) {
            when (val result = client.getDownloadInfo(id)) {
                is DesktopResult.Success -> {
                    // Server returns "null" (raw, no quotes) for missing downloads
                    if (result.data == "null") {
                        term.println((TextColors.red)("Download #$id not found"))
                    } else {
                        try {
                            val item = jsonParser.decodeFromString(ApiDownloadModel.serializer(), result.data)
                            foundItems.add(item)
                        } catch (e: Exception) {
                            term.println((TextColors.red)("Failed to parse response for #$id: ${e.message}"))
                        }
                    }
                }
                is DesktopResult.ConnectionError -> {
                    System.err.println("Connection error for #$id: ${result.message}")
                }
                is DesktopResult.RemoteError -> {
                    System.err.println("Server error for #$id (${result.statusCode}): ${result.body}")
                }
            }
        }

        if (foundItems.isEmpty()) {
            if (jsonOutput) {
                term.println("[]")
            }
            return
        }

        if (jsonOutput) {
            term.println(jsonParser.encodeToString(ListSerializer(ApiDownloadModel.serializer()), foundItems))
        } else {
            for (item in foundItems) {
                term.println((TextColors.brightCyan)("Download #${item.id}"))
                term.println((TextColors.brightBlue)("  Name:    ") + item.name)
                term.println((TextColors.brightBlue)("  URL:     ") + item.url)
                term.println((TextColors.brightBlue)("  Folder:  ") + item.folder)
                val statusColor = when (item.status) {
                    "Completed" -> TextColors.green
                    "Error" -> TextColors.red
                    "Downloading" -> TextColors.cyan
                    "Paused" -> TextColors.yellow
                    else -> TextColors.white
                }
                term.println((TextColors.brightBlue)("  Status:  ") + statusColor(item.status))
                term.println((TextColors.brightBlue)("  Size:    ") + CliFormatting.formatSize(item.size))
                term.println((TextColors.brightBlue)("  Downloaded: ") + CliFormatting.formatSize(item.downloaded))

                // Speed and progress are currently hardcoded to 0 in the API.
                // ApiDownloadModel.kt defines speed and progress fields, but
                // IntegrationHandlerImp.toApiModel() uses hardcoded 0 values
                // and never queries the live monitor layer for real progress data.
                // (IntegrationHandlerImp.kt:128-144)
                // To restore speed/progress display, update the server-side
                // toApiModel() to read from ProcessingDownloadItemState.

                // if (item.speed > 0) {
                //     term.println((TextColors.brightBlue)("  Speed:   ") + CliFormatting.formatSpeed(item.speed))
                // }
                // if (item.progress > 0) {
                //     term.println((TextColors.brightBlue)("  Progress: ") + "%.1f%%".format(item.progress))
                // }

                term.println((TextColors.brightBlue)("  Added:   ") + CliFormatting.formatTimestamp(item.dateAdded))
                val startTime = item.startTime
                if (startTime != null && startTime > 0) {
                    term.println((TextColors.brightBlue)("  Started: ") + CliFormatting.formatTimestamp(startTime))
                }
                val completeTime = item.completeTime
                if (completeTime != null && completeTime > 0) {
                    term.println((TextColors.brightBlue)("  Completed: ") + CliFormatting.formatTimestamp(completeTime))
                }
                if (item.connections != null) {
                    term.println((TextColors.brightBlue)("  Con——nections: ") + item.connections.toString())
                }
                if (item.speedLimit > 0) {
                    term.println((TextColors.brightBlue)("  Speed limit: ") + CliFormatting.formatSpeed(item.speedLimit))
                }
                if (item.checksum != null) {
                    term.println((TextColors.brightBlue)("  Checksum: ") + item.checksum)
                }
                term.println()
            }
        }
    }
}