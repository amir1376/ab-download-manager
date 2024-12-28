package com.abdownloadmanager.updateapplier

import java.io.File

class UpdateInstallerByWindowsExecutable(
    private val executable: File,
) : UpdateInstaller {
    override fun installUpdate() {
        val file = executable.absolutePath
        ProcessBuilder()
            .command("cmd", "/c", file, "/S")
            .start()
    }
}