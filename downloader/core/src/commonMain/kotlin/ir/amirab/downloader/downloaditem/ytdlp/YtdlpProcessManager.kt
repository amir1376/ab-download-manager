package ir.amirab.downloader.downloaditem.ytdlp

import ir.amirab.util.platform.Platform
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YtdlpProcessManager {
    private var exePathProvider: (() -> String)? = null

    fun init(provider: () -> String) {
        exePathProvider = provider
    }

    fun getExePath(): String {
        return exePathProvider?.invoke() ?: "yt-dlp"
    }

    private fun isFfmpegAvailable(ytDlpDir: File): Boolean {
        val platform = Platform.getCurrentPlatform()
        val ffmpegExeName = if (platform == Platform.Desktop.Windows) "ffmpeg.exe" else "ffmpeg"
        
        // 1. Check if ffmpeg exists in the same directory as yt-dlp
        if (File(ytDlpDir, ffmpegExeName).exists()) {
            return true
        }
        
        // 2. Check if ffmpeg is in system PATH
        return try {
            val process = ProcessBuilder(ffmpegExeName, "-version").start()
            process.destroy()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun ensureExecutableInstalled(onProgress: (Double) -> Unit = {}): File {
        val exePath = getExePath()
        val file = File(exePath)
        val targetDir = file.parentFile ?: File(".")
        
        val needYtdlp = !file.exists()
        val needFfmpeg = Platform.getCurrentPlatform() == Platform.Desktop.Windows && !isFfmpegAvailable(targetDir)

        if (needYtdlp || needFfmpeg) {
            withContext(Dispatchers.IO) {
                targetDir.mkdirs()
                
                if (needYtdlp) {
                    val platform = Platform.getCurrentPlatform()
                    val urlString = when (platform) {
                        Platform.Desktop.Windows -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
                        Platform.Desktop.MacOS -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos"
                        else -> "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
                    }
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    val totalSize = connection.contentLengthLong

                    BufferedInputStream(connection.inputStream).use { input ->
                        FileOutputStream(file).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                if (totalSize > 0) {
                                    val progress = totalBytesRead.toDouble() / totalSize
                                    if (needFfmpeg) {
                                        onProgress(0.2 * progress)
                                    } else {
                                        onProgress(progress)
                                    }
                                }
                            }
                        }
                    }
                    file.setExecutable(true)
                }

                if (needFfmpeg) {
                    val zipUrl = URL("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip")
                    val zipConnection = zipUrl.openConnection() as HttpURLConnection
                    zipConnection.connectTimeout = 15000
                    zipConnection.readTimeout = 15000
                    val totalZipSize = zipConnection.contentLengthLong

                    val tempZipFile = File(targetDir, "ffmpeg_temp.zip")
                    try {
                        BufferedInputStream(zipConnection.inputStream).use { input ->
                            FileOutputStream(tempZipFile).use { output ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var totalBytesRead = 0L
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    totalBytesRead += bytesRead
                                    if (totalZipSize > 0) {
                                        val progress = totalBytesRead.toDouble() / totalZipSize
                                        if (needYtdlp) {
                                            onProgress(0.2 + 0.8 * progress)
                                        } else {
                                            onProgress(progress)
                                        }
                                    }
                                }
                            }
                        }

                        // Now extract ffmpeg.exe and ffprobe.exe
                        FileInputStream(tempZipFile).use { fileInputStream ->
                            ZipInputStream(BufferedInputStream(fileInputStream)).use { zipInputStream ->
                                var entry = zipInputStream.nextEntry
                                while (entry != null) {
                                    val name = entry.name
                                    if (name.endsWith("ffmpeg.exe") || name.endsWith("ffprobe.exe")) {
                                        val baseName = if (name.endsWith("ffmpeg.exe")) "ffmpeg.exe" else "ffprobe.exe"
                                        val destFile = File(targetDir, baseName)
                                        FileOutputStream(destFile).use { outStream ->
                                            val buffer = ByteArray(8192)
                                            var bytesRead: Int
                                            while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                                                outStream.write(buffer, 0, bytesRead)
                                            }
                                        }
                                        destFile.setExecutable(true)
                                    }
                                    zipInputStream.closeEntry()
                                    entry = zipInputStream.nextEntry
                                }
                            }
                        }
                    } finally {
                        if (tempZipFile.exists()) {
                            tempZipFile.delete()
                        }
                    }
                }
            }
        }
        return file
    }
}
