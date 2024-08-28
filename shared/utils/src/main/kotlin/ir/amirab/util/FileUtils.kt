package ir.amirab.util

import ir.amirab.util.platform.Platform
import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException

interface FileUtils {
    fun openFile(file: File)
    fun openFolderOfFile(file: File): Boolean
    fun canWriteInThisFolder(folder: String): Boolean

    companion object : FileUtils by getPlatformFileUtil()
}

private fun getPlatformFileUtil(): FileUtils {
    return when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Windows -> WindowsFileUtils()
        Platform.Desktop.Linux -> LinuxFileUtils()
        Platform.Desktop.MacOS -> MacOsFileUtils()
        Platform.Android -> DefaultFileUtils()
    }
}

// it's better to move this to another module (or at least create multiple files) if it's going to be more complicated
/**
 * it uses the jvm default and can be overridden to use fallback if jvm can't handle that
 */
private open class DefaultFileUtils : FileUtils {
    override fun openFile(file: File) {
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        val desktop = Desktop.getDesktop() ?: return;
        desktop.open(file)
    }

    override fun openFolderOfFile(file: File): Boolean {
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        runCatching {
            Desktop.getDesktop().browseFileDirectory(file)
            return true
        }
        return fallBackOpenFolderOfFile(file.absoluteFile)
    }

    /**
     * @param file it must be an absolute file!
     */
    open fun fallBackOpenFolderOfFile(file: File): Boolean {
        return false
    }

    override fun canWriteInThisFolder(folder: String): Boolean {
        return runCatching {
            File(folder).canUseThisAsFolder()
        }.getOrElse { false }
    }

    private fun File.canUseThisAsFolder(): Boolean {
        var current: File? = this
        while (true) {
            if (current == null) break
            if (current.exists()) {
                return current.isDirectory
            }
            current = current.parentFile
        }
        return false
    }
}

private class WindowsFileUtils : DefaultFileUtils() {
    override fun fallBackOpenFolderOfFile(file: File): Boolean {
        return kotlin.runCatching {
            Runtime.getRuntime()
                .exec(
                    arrayOf(
                        "cmd", "/c",
                        "explorer.exe",
                        "/select,",
                        file.path
                    )
                ).exitValue() == 0
        }.getOrElse { false }
    }
}

private class LinuxFileUtils : DefaultFileUtils() {
    override fun fallBackOpenFolderOfFile(file: File): Boolean {
        val dbusSendResult = kotlin.runCatching {
            Runtime.getRuntime().exec(
                arrayOf(
                    "dbus-send",
                    "--print-reply",
                    "--dest=org.freedesktop.FileManager1",
                    "/org/freedesktop/FileManager1",
                    "org.freedesktop.FileManager1.ShowItems",
                    "array:string:file://${file.path}",
                    "string:"
                )
            ).exitValue() == 0
        }.getOrElse { false }
        if (dbusSendResult) {
            return true
        }
        val xdgOpenResult = kotlin.runCatching {
            Runtime.getRuntime().exec(arrayOf("xdg-open", file.parent)).exitValue() == 0
        }.getOrElse { false }
        return xdgOpenResult
    }
}

private class MacOsFileUtils : DefaultFileUtils() {
    override fun fallBackOpenFolderOfFile(file: File): Boolean {
        return kotlin.runCatching {
            Runtime.getRuntime()
                .exec(arrayOf("open", "-R", file.path)).exitValue() == 0
        }.getOrElse { false }
    }
}