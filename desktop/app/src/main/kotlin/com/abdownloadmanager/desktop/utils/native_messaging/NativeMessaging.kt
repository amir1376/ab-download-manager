package com.abdownloadmanager.desktop.utils.native_messaging

import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.shared.util.SharedConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    fun boot() {
        installManifests()
    }

    fun installManifests() {
        val execFile = AppInfo.nativeMessagingExeFile ?: return
        val firefox = createFirefoxManifest(execFile)
        val chrome = createChromeManifest(execFile)
        nativeMessagingManifestApplier.updateManifests(
            NativeMessagingManifests(
                firefoxNativeMessagingManifest = firefox,
                chromeNativeMessagingManifest = chrome,
            )
        )
    }

    private fun createFirefoxManifest(
        execFile: String
    ): FirefoxNativeMessagingManifest {
        return FirefoxNativeMessagingManifest(
            name = AppInfo.displayName,
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
            name = AppInfo.displayName,
            description = AppInfo.displayName,
            path = execFile,
            type = "stdio",
            allowedOrigins = listOf(
                SharedConstants.chromeExtensionOrigin
            )
        )
    }
}
