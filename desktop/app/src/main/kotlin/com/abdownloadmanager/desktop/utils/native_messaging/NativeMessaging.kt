package com.abdownloadmanager.desktop.utils.native_messaging

import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.isAppInstalled
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    fun boot(){
        installManifests()
    }
    fun installManifests() {
        val firefox = createFirefoxManifest()
        val chrome = createChromeManifest()
        if (chrome!=null && firefox!=null){
            nativeMessagingManifestApplier.updateManifests(
                NativeMessagingManifests(
                    firefoxNativeMessagingManifest = firefox,
                    chromeNativeMessagingManifest = chrome,
                )
            )
        }
    }

    private fun createFirefoxManifest(): FirefoxNativeMessagingManifest? {
        if (!AppInfo.isAppInstalled()) return null
        val execFile = AppInfo.exeFile!!
        return FirefoxNativeMessagingManifest(
            name = AppInfo.name,
            description = AppInfo.name,
            path = execFile,
            type = "stdio",
            allowedExtensions = listOf(
                ""
            )
        )
    }
    private fun createChromeManifest(): ChromeNativeMessagingManifest? {
        if (!AppInfo.isAppInstalled()) return null
        val execFile = AppInfo.exeFile!!
        return ChromeNativeMessagingManifest(
            name = AppInfo.name,
            description = AppInfo.name,
            path = execFile,
            type = "stdio",
            allowedOrigins = listOf(
                ""
            )
        )
    }
}