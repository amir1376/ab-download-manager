package com.abdownloadmanager

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Platform

data class AppArtifactInfo(
    val version: Version,
    val platform: Platform,
)

object ArtifactUtil {
    private val versionPatern = "(\\d+\\.\\d+\\.\\d+)"
    val versionRegex = "_$versionPatern".toRegex()
    val platformRegex = "_${versionPatern}_([a-zA-Z]+)".toRegex()
    fun extractVersion(name: String): Version? {
        versionRegex.toString()
        val versionString = versionRegex.find(name)?.groupValues?.get(1) ?: return null
        return Version.parse(versionString)
    }

    fun extractVersionFromTag(tagName: String): Version? {
        return versionRegex.find(tagName)?.value?.let {
            Version.parse(it)
        }
    }

    private fun extractPlatformFromName(name: String): Platform? {
        val platformString = platformRegex.find(name)?.groupValues?.get(2) ?: return null
        return Platform.fromString(platformString)
    }


    fun getArtifactInfo(name: String): AppArtifactInfo? {
        val version = extractVersion(name) ?: return null
        val platform = extractPlatformFromName(name) ?: return null
        return AppArtifactInfo(
            version = version,
            platform = platform,
        )


    }

}