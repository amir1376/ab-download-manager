package com.abdownloadmanager.shared.util.downloadlocation

import java.io.File
import java.io.Reader
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

    private fun getUserDirsFile(): File {
        val xdgConfigFromEnv: File? = System.getenv("XDG_CONFIG_HOME")
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
        val configDir = (xdgConfigFromEnv ?: File(System.getProperty("user.home"), ".config"))
        return File(configDir, "user-dirs.dirs")
    }

    private fun getConfigFileReader(): Reader? {
        return getUserDirsFile()
            .takeIf { it.exists() }
            ?.bufferedReader(Charsets.UTF_8)
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
