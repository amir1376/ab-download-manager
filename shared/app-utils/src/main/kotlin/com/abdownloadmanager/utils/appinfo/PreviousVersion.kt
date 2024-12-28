package com.abdownloadmanager.utils.appinfo

import io.github.z4kn4fein.semver.Version
import java.io.File

class PreviousVersion(
    systemPath: File,
    private val currentVersion: Version,
) {
    private val versionFile = File(systemPath, ".version")
    private var previousVersion: Version? = null
    fun get(): Version? {
        return previousVersion
    }

    fun boot() {
        previousVersion = kotlin.runCatching {
            // maybe versionFile is null but we catch it
            val versionString = versionFile.readText()
            Version.parse(versionString)
        }.getOrNull()
        kotlin.runCatching {
            versionFile.parentFile.mkdirs()
            versionFile.writeText(currentVersion.toString())
        }.onFailure {
            it.printStackTrace()
        }
    }
}