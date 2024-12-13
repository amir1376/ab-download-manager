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
        return getLatestVersionsForThisDevice()
    }

    private suspend fun getLatestVersionsForThisDevice(): UpdateInfo {
        val release = githubApi.getLatestReleases()
        val currentPlatform = Platform.getCurrentPlatform()
        val currentArch = Arch.getCurrentArch()
        val updateSources = mutableListOf<UpdateSource>()
        var foundVersion: Version? = null
        var initializedVersionFromAssetNames = false
        for (asset in release.assets) {
            val v = ArtifactUtil.getArtifactInfo(asset.name) ?: continue
            if (v.platform != currentPlatform) continue
            if (v.arch != currentArch) continue
            if (!initializedVersionFromAssetNames) {
                foundVersion = v.version
                initializedVersionFromAssetNames = true
            }
            val isHashFile = asset.name.endsWith(".md5")
            if (isHashFile) {
                // nothing for now!
            } else {
                updateSources.add(
                    UpdateSource.DirectDownloadLink(
                        asset.downloadLink,
                        asset.name,
                        null,
                    )
                )
            }
        }
        return UpdateInfo(
            version = foundVersion
                ?: Version.parse(release.version.substring("v".length)),
            platform = currentPlatform,
            arch = currentArch,
            changeLog = release.body ?: "",
            updateSource = updateSources
        )
    }
}