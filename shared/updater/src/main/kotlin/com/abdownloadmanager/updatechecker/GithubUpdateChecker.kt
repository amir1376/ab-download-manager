package com.abdownloadmanager.updatechecker

import GithubApi
import com.abdownloadmanager.ArtifactUtil
import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform

class GithubUpdateChecker(
    currentVersion: Version,
    private val githubApi: GithubApi,
) : UpdateChecker(currentVersion) {
    override suspend fun getMyPlatformLatestVersion(): UpdateInfo {
        val all = getLatestVersions()
        val currentPlatform = Platform.getCurrentPlatform()
        val currentArch = Arch.getCurrentArch()
        val versionData = all.filter {
            it.platform == currentPlatform && it.arch == currentArch
        }.firstOrNull {
            it.name.run {
                endsWith(".tar.gz") || endsWith(".zip")
            }
        }
        return requireNotNull(versionData) {
            "could not find latest version for current platform"
        }
    }

    suspend fun getLatestVersions(): List<UpdateInfo> {
        val release = githubApi.getLatestReleases()
        return release.assets.mapNotNull {
            val v = ArtifactUtil.getArtifactInfo(it.name) ?: return@mapNotNull null
            UpdateInfo(
                name = it.name,
                version = v.version,
                link = it.downloadLink,
                platform = v.platform,
                arch = v.arch,
                changeLog = release.body ?: ""
            )
        }
    }
}