package ir.amirab.util

import ir.amirab.util.platform.Platform
import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

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
        val desktop = Desktop.getDesktop() ?: return
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
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", "/select,", file.path))
    }
}

private class LinuxFileUtils : DefaultFileUtils() {
    override fun fallBackOpenFolderOfFile(file: File): Boolean {
        val dbusSendResult = execAndWait(
            arrayOf(
                "dbus-send",
                "--print-reply",
                "--dest=org.freedesktop.FileManager1",
                "/org/freedesktop/FileManager1",
                "org.freedesktop.FileManager1.ShowItems",
                "array:string:file://${file.path}",
                "string:"
            )
        )
        if (dbusSendResult) {
            return true
        }
        val xdgOpenResult = execAndWait(
            arrayOf("xdg-open", file.parent)
        )
        return xdgOpenResult
    }
}

private class MacOsFileUtils : DefaultFileUtils() {
    override fun fallBackOpenFolderOfFile(file: File): Boolean {
        return execAndWait(arrayOf("open", "-R", file.path))
    }
}

/**
 * this helper function is here to execute a command and waits for the process to finish and return the result based on exit code
 * @param command the command
 * @param waitFor maximum time allowed process finish ( in milliseconds )
 * @return `true` when process exits with `0` exit code, `false` if the process fails with non-zero exit code or execution time exceeds the [waitFor]
 */
private fun execAndWait(
    command: Array<String>,
    waitFor: Long = 2_000,
): Boolean {
    return runCatching {
        val p = Runtime.getRuntime().exec(command)
        val exited = p.waitFor(waitFor, TimeUnit.MILLISECONDS)
        if (exited) {
            p.exitValue() == 0
        } else {
            false
        }
    }.getOrElse { false }
}