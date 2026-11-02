package com.abdownloadmanager.desktop.nativemessaging

import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.shared.util.SharedConstants
import io.ktor.util.Platform
import ir.amirab.util.logger.thisLogger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

data class NativeMessagingManifests(
    val firefoxNativeMessagingManifest: FirefoxNativeMessagingManifest,
    val chromeNativeMessagingManifest: ChromeNativeMessagingManifest,
)


@Serializable
data class FirefoxNativeMessagingManifest(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("path")
    val path: String,
    @SerialName("type")
    val type: String,
    @SerialName("allowed_extensions")
    val allowedExtensions: List<String>,
)

@Serializable
data class ChromeNativeMessagingManifest(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("path")
    val path: String,
    @SerialName("type")
    val type: String,
    @SerialName("allowed_origins")
    val allowedOrigins: List<String>,
)


class NativeMessaging(
    private val nativeMessagingManifestApplier: NativeMessagingManifestApplier,
) {
    val logger = thisLogger()
    fun boot() {
        try {
            installManifests()
        } catch (e: Exception) {
            logger.e(e) { "can't install native messaging manifests" }
        }
    }
    fun installManifests() {
        val execFile = getNativeMessagingExeFile() ?: return
        val firefox = createFirefoxManifest(execFile)
        val chrome = createChromeManifest(execFile)
        nativeMessagingManifestApplier.updateManifests(
            NativeMessagingManifests(
                firefoxNativeMessagingManifest = firefox,
                chromeNativeMessagingManifest = chrome,
            )
        )
    }

    private fun getNativeMessagingExeFile(): String? {
        val default = AppInfo.nativeMessagingExeFile ?: return null
        val info = File("/.flatpak-info").takeIf { it.exists() } ?: return default
        val base = Regex("^(/.*?)/app/").find(
            runCatching {
                Properties().apply { info.inputStream().use(::load) }.getProperty("app-path")
            }.getOrNull().orEmpty()
        )?.groupValues?.get(1) ?: "/var/lib/flatpak"

        return "$base/exports/bin/com.abdownloadmanager.abdownloadmanager"
    }

    fun uninstallManifests() {
        nativeMessagingManifestApplier.removeManifests()
    }

    private fun createFirefoxManifest(
        execFile: String
    ): FirefoxNativeMessagingManifest {
        return FirefoxNativeMessagingManifest(
            name = AppInfo.packageName,
            description = AppInfo.displayName,
            path = execFile,
            type = "stdio",
            allowedExtensions = listOf(
                SharedConstants.firefoxExtensionId
            )
        )
    }

    private fun createChromeManifest(
        execFile: String,
    ): ChromeNativeMessagingManifest {
        return ChromeNativeMessagingManifest(
            name = AppInfo.packageName,
            description = AppInfo.displayName,
            path = execFile,
            type = "stdio",
            allowedOrigins = listOf(
                SharedConstants.chromeExtensionOrigin
            )
        )
    }

    companion object {
        fun getDefault(json: Json): NativeMessaging {
            return NativeMessaging(NativeMessagingManifestApplier.getForCurrentPlatform(json))
        }
    }
}
