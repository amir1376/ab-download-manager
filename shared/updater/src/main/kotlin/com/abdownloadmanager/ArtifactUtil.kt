package com.abdownloadmanager

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform

data class AppArtifactInfo(
    val version: Version,
    val platform: Platform,
    val arch: Arch,
)

object ArtifactUtil {
    val artifactRegex =
        "(?<appName>[a-zA-Z]+)_(?<version>(\\d+\\.\\d+\\.\\d+))_(?<platform>[a-zA-Z]+)_(?<arch>[a-zA-Z0-9]+)\\.(?<extension>.+)".toRegex()

    fun getArtifactInfo(name: String): AppArtifactInfo? {
        val values = artifactRegex.find(name)?.groups ?: return null
        val version = runCatching { values.get("version")?.value }
            .getOrNull()
            ?.let(Version::parse)
            ?: return null
        val platform = runCatching { values.get("platform")?.value }
            .getOrNull()
            ?.let(Platform::fromString)
            ?: return null
        val arch = runCatching { values.get("arch")?.value }
            .getOrNull()
            ?.let(Arch::fromString)
            ?: return null
        return AppArtifactInfo(
            version = version,
            platform = platform,
            arch = arch,
        )
    }
}