package ir.amirab.util.desktop.downloadlocation

import java.io.File
import java.io.InputStream
import java.util.Properties

class LinuxDownloadLocationProvider : DesktopDownloadLocationProvider() {
    override fun getCurrentDownloadLocation(): File? {
        val properties = getConfigFileReader()?.use {
            Properties().apply {
                load(it)
            }
        }
        return properties
            ?.getWithResolvedVariables("XDG_DOWNLOAD_DIR")
            ?.let(::File)
            ?.canonicalFile
    }

    private fun getConfigFileReader(): InputStream? {
        val configFile = File(System.getProperty("user.home"), ".config/user-dirs.dirs")
        if (configFile.exists()) {
            return configFile.inputStream().buffered()
        }
        return null
    }

    private fun Properties.getWithResolvedVariables(key: String): String? {
        return runCatching {
            getProperty(key)
                ?.hydrateEnvVariables()
                ?.trim('"')
                ?.trim('\'')
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

    private val variableRegex = "\\$\\{?([A-Za-z0-9_]+)\\}?".toRegex()
    private fun String.hydrateEnvVariables(): String {
        return variableRegex.replace(this) {
            val variableName = it.groupValues[1]
            System.getenv(variableName)
        }
    }
}
