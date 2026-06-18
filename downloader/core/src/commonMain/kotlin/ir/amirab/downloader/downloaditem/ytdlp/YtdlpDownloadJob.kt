package ir.amirab.downloader.downloaditem.ytdlp

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.destination.SimpleDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import ir.amirab.util.platform.Platform
import java.util.concurrent.atomic.AtomicLong

class YtdlpDownloadJob(
    override val downloadItem: YtdlpDownloadItem,
    downloadManager: DownloadManager,
    private val ytdlpExecutablePathProvider: () -> String,
) : DownloadJob(
    downloadManager = downloadManager,
) {
    private lateinit var destination: SimpleDownloadDestination
    private var process: Process? = null
    private val downloadedBytes = AtomicLong(0L)

    override fun getDestination(): DownloadDestination {
        return destination
    }

    override suspend fun actualBoot() {
        initializeDestination()
    }

    override fun initializeDestination() {
        val outFile = downloadManager.calculateOutputFile(downloadItem)
        destination = SimpleDownloadDestination(
            file = outFile,
            emptyFileCreator = downloadManager.emptyFileCreator,
            appendExtensionToIncompleteDownloads = downloadManager.settings.appendExtensionToIncompleteDownloads,
            downloadId = id
        )
    }

    override suspend fun reset() {
        pause()
        downloadItem.contentLength = -1L
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        downloadedBytes.set(0)
        saveState()
        downloadManager.onDownloadItemChange(downloadItem)
    }

    override suspend fun resume() {
        if (isDownloadActive.value) {
            return
        }
        _isDownloadActive.update { true }

        val activeScope = newScopeBasedOn(scope)
        activeDownloadScope = activeScope

        activeScope.launch {
            boot()
            onDownloadResuming()
            try {
                _status.value = DownloadJobStatus.PreparingFile(0)
                val exeFile = YtdlpProcessManager.ensureExecutableInstalled { progress ->
                    _status.value = DownloadJobStatus.PreparingFile((progress * 100).toInt())
                }
                val exePath = exeFile.absolutePath

                // Ensure the filename extension is .mp4
                val currentName = downloadItem.name
                val ext = currentName.substringAfterLast('.', "")
                if (ext.lowercase() in listOf("webm", "mkv", "3gp", "flv", "ogg", "avi", "mov", "wmv", "")) {
                    val baseName = if (ext.isEmpty()) currentName else currentName.substringBeforeLast('.')
                    downloadItem.name = "$baseName.mp4"
                    saveState()
                    initializeDestination()
                }

                File(downloadItem.folder).mkdirs()

                downloadItem.status = DownloadStatus.Downloading
                if (downloadItem.startTime == null) {
                    downloadItem.startTime = System.currentTimeMillis()
                }
                saveState()
                onDownloadResumed()

                withContext(Dispatchers.IO) {
                    val ffmpegDir = exeFile.parentFile?.absolutePath ?: File(".").absolutePath
                    val pb = ProcessBuilder(
                        exePath,
                        "--ffmpeg-location", ffmpegDir,
                        "--remux-video", "mp4",
                        "--merge-output-format", "mp4",
                        "--newline",
                        "--progress",
                        "--no-playlist",
                        "-o", File(downloadItem.folder, downloadItem.name).absolutePath,
                        downloadItem.link
                    )
                    pb.redirectErrorStream(true)
                    val proc = pb.start()
                    process = proc

                    val reader = BufferedReader(InputStreamReader(proc.inputStream))
                    var line: String?

                    val percentRegex = """([\d.]+)%""".toRegex()
                    val speedRegex = """at\s+([\d.]+)(\w+)/s""".toRegex()
                    val sizeRegex = """of\s+~?([\d.]+)(\w+)""".toRegex()

                    val outputLog = mutableListOf<String>()
                    while (reader.readLine().also { line = it } != null) {
                        ensureActive()
                        val l = line!!
                        outputLog.add(l)
                        if (outputLog.size > 100) {
                            outputLog.removeAt(0)
                        }
                        if (l.contains("%")) {
                            val percentMatch = percentRegex.find(l)
                            if (percentMatch != null) {
                                val percent = percentMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                                val sizeMatch = sizeRegex.find(l)
                                if (sizeMatch != null) {
                                    val sizeVal = sizeMatch.groupValues[1]
                                    val sizeUnit = sizeMatch.groupValues[2]
                                    val totalBytes = parseSizeToBytes(sizeVal, sizeUnit)
                                    if (totalBytes > 0 && downloadItem.contentLength != totalBytes) {
                                        downloadItem.contentLength = totalBytes
                                        saveState()
                                    }
                                    downloadedBytes.set((percent * totalBytes / 100.0).toLong())
                                }
                            }
                        }
                    }
                    val exitCode = proc.waitFor()
                    if (exitCode != 0) {
                        val errorLog = outputLog.joinToString("\n")
                        throw Exception("yt-dlp exited with non-zero exit code: $exitCode. Output:\n$errorLog")
                    }
                }
                onDownloadFinished()
            } catch (e: Exception) {
                onDownloadCanceled(e)
            } finally {
                process?.destroy()
                process = null
            }
        }
    }

    private fun parseSizeToBytes(valueStr: String, unit: String): Long {
        val value = valueStr.toDoubleOrNull() ?: return -1
        return when (unit.uppercase()) {
            "GB", "GIB" -> (value * 1024 * 1024 * 1024).toLong()
            "MB", "MIB" -> (value * 1024 * 1024).toLong()
            "KB", "KIB" -> (value * 1024).toLong()
            "B" -> value.toLong()
            else -> -1
        }
    }

    override suspend fun pause(throwable: Throwable) {
        activeDownloadScope?.cancel()
        process?.destroy()
        process = null
        _isDownloadActive.update { false }
        onDownloadCanceled(throwable)
    }

    override suspend fun saveState() {
        downloadManager.dlListDb.update(downloadItem)
    }

    override fun getDownloadedSize(): Long {
        return downloadedBytes.get()
    }

    override fun reloadSettings() {
    }

    override suspend fun changeConfig(
        updater: (IDownloadItem) -> Unit,
        extraConfig: DownloadJobExtraConfig?
    ): IDownloadItem {
        updater(downloadItem)
        saveState()
        return downloadItem
    }

    override suspend fun extraConfigsReceived(config: DownloadJobExtraConfig) {
    }
}
