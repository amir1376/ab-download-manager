package ir.amirab.util

import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException

object FileUtils {
    fun openFile(file: File) {
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        val desktop = Desktop.getDesktop() ?: return;
        desktop.open(file)
    }

    fun openFolderOfFile(file: File):Boolean {
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        runCatching {
            Desktop.getDesktop().browseFileDirectory(file)
            return true
        }
        val os = System.getProperty("os.name").lowercase()

        val cmd=when {
            os.contains("win") -> {
                arrayOf("explorer.exe", "/select,", file.absolutePath)
            }

            os.contains("mac") -> {
                arrayOf("open", "-R", file.absolutePath)
            }

            os.contains("nix") || os.contains("nux") || os.contains("mac") -> {
                arrayOf("xdg-open", file.absolutePath)
            }

            else -> null
        }
        return runCatching {
            if (cmd!=null){
                val r=Runtime.getRuntime().exec(cmd)
                r.exitValue() == 0
            }else{
                false
            }
        }.getOrElse { false }
    }


    fun canWriteInThisFolder(folder:String): Boolean {
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
