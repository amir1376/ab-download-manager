package com.abdownloadmanager.desktop.clipboard

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

data class DetectedUrl(
    val url: String,
    val suggestedFilename: String?,
    val timestamp: Long,
)

class ClipboardMonitor(
    private val scope: CoroutineScope,
    private val pollingIntervalMs: Long = 1500L,
    private val deduplicateWindowMs: Long = 30_000L,
) {
    private val _urlDetected = MutableSharedFlow<DetectedUrl>()
    val urlDetected: SharedFlow<DetectedUrl> = _urlDetected.asSharedFlow()

    @Volatile
    private var monitorJob: Job? = null

    @Volatile
    private var running = false

    private var lastDetectedUrl: String? = null
    private var lastDetectedTime: Long = 0L

    fun start() {
        if (running) return
        running = true
        monitorJob = scope.launch(Dispatchers.IO) {
            while (isActive && running) {
                try {
                    val clipText = readClipboard()
                    if (clipText != null && isValidUrl(clipText)) {
                        val now = System.currentTimeMillis()
                        val isDuplicate = clipText == lastDetectedUrl &&
                                (now - lastDetectedTime) < deduplicateWindowMs
                        if (!isDuplicate) {
                            lastDetectedUrl = clipText
                            lastDetectedTime = now
                            val filename = extractFilenameFromUrl(clipText)
                            _urlDetected.emit(
                                DetectedUrl(
                                    url = clipText.trim(),
                                    suggestedFilename = filename,
                                    timestamp = now,
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                    // Clipboard may be unavailable, skip this cycle
                }
                delay(pollingIntervalMs)
            }
        }
    }

    fun stop() {
        running = false
        monitorJob?.cancel()
        monitorJob = null
    }

    fun isRunning(): Boolean = running

    private fun readClipboard(): String? {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                clipboard.getData(DataFlavor.stringFlavor) as? String
            } else null
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val URL_PATTERN = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)

        fun isValidUrl(text: String): Boolean {
            val trimmed = text.trim()
            return trimmed.length < 2048 && URL_PATTERN.matches(trimmed)
        }

        fun extractFilenameFromUrl(url: String): String? {
            return try {
                val path = java.net.URI(url.trim()).path ?: return null
                val lastSegment = path.substringAfterLast('/')
                if (lastSegment.isNotBlank() && lastSegment.contains('.')) {
                    lastSegment
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
