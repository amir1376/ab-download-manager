package com.abdownloadmanager.desktop.utils.native_messaging

import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.AppProperties
import com.abdownloadmanager.desktop.utils.configDir
import com.abdownloadmanager.desktop.utils.isAppInstalled
import ir.amirab.util.platform.Platform
import ir.amirab.util.desktop.WindowsRegistry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.io.path.*

abstract class NativeMessagingManifestApplier : KoinComponent {
    protected val json by inject<Json>()
    protected inline fun <reified T : Any> serialize(data: T): String {
        return json.encodeToString(data)
    }

    protected inline fun <reified T : Any> deserialize(string: String): T {
        return json.decodeFromString(string)
    }

    abstract fun updateManifests(manifests: NativeMessagingManifests)
    abstract fun removeManifests()

    companion object {
        fun getForCurrentPlatform(): NativeMessagingManifestApplier {
            if (!AppInfo.isAppInstalled()){
                return NoOpNativeMessagingApplier()
            }
            return when(AppInfo.platform){
                Platform.Desktop.Linux -> LinuxNativeMessagingManifestApplier()
                Platform.Desktop.MacOS -> MacosNativeMessagingManifestApplier()
                Platform.Desktop.Windows -> WindowsNativeMessagingManifestApplier()
                Platform.Android -> error("there is no native messaging for android so this code should never used in android")
            }
        }
    }
}

class WindowsNativeMessagingManifestApplier : NativeMessagingManifestApplier() {
    private val baseNativeMessagingDir get() =  AppInfo.configDir.toPath() / "native-messaging"
    private val firefoxManifestFile get() = baseNativeMessagingDir / "firefox-native-messaging-manifest.json"
    private val chromeManifestFile get() = baseNativeMessagingDir / "chrome-native-messaging-manifest.json"
    private val firefoxRegistryPath get() = "HKCU\\SOFTWARE\\Mozilla\\NativeMessagingHosts\\${AppInfo.packageName}"
    private val chromeRegistryPath get() = "HKCU\\SOFTWARE\\Google\\Chrome\\NativeMessagingHosts\\${AppInfo.packageName}"

    override fun updateManifests(
        manifests: NativeMessagingManifests
    ) {
        listOf(
            firefoxManifestFile,
            chromeManifestFile,
        ).forEach { it.createParentDirectories() }
        firefoxManifestFile.writeText(serialize(manifests.firefoxNativeMessagingManifest))
        WindowsRegistry.setValueInRegistry(
            path = firefoxRegistryPath,
            key = null,
            value = firefoxManifestFile.toString()
        )
        chromeManifestFile.writeText(serialize(manifests.chromeNativeMessagingManifest))
        WindowsRegistry.setValueInRegistry(
            path = chromeRegistryPath,
            key = null,
            value = chromeManifestFile.toString()
        )
    }

    override fun removeManifests() {
        firefoxManifestFile.deleteIfExists()
        WindowsRegistry.removePathInRegistry(
            path = firefoxRegistryPath,
        )
        chromeManifestFile.deleteIfExists()
        WindowsRegistry.removePathInRegistry(
            path = chromeRegistryPath,
        )
    }

}

class MacosNativeMessagingManifestApplier : NativeMessagingManifestApplier() {
    private val firefoxNativeMessagingPath
        get() = Path(AppProperties.userDir, "Library/Application Support/Mozilla/NativeMessagingHosts",
            "${AppInfo.packageName}.json"
        )
    private val chromeNativeMessagingPath
        get() = Path(AppProperties.userDir, "Library/Application Support/Google/Chrome/NativeMessagingHosts",
            "${AppInfo.packageName}.json"
        )
    private val chromiumNativeMessagingPath
        get() = Path(AppProperties.userDir, "Library/Application Support/Chromium/NativeMessagingHosts",
            "${AppInfo.packageName}.json"
        )



    override fun updateManifests(manifests: NativeMessagingManifests) {
        listOf(
            firefoxNativeMessagingPath,
            chromeNativeMessagingPath,
            chromiumNativeMessagingPath
        ).forEach { it.createParentDirectories() }

        firefoxNativeMessagingPath.writeText(serialize(manifests.firefoxNativeMessagingManifest))
        val chromeManifestString=serialize(manifests.chromeNativeMessagingManifest)
        chromeNativeMessagingPath.writeText(chromeManifestString)
        chromiumNativeMessagingPath.writeText(chromeManifestString)
    }

    override fun removeManifests() {
        firefoxNativeMessagingPath.deleteIfExists()
        chromeNativeMessagingPath.deleteIfExists()
        chromiumNativeMessagingPath.deleteIfExists()
    }
}

class LinuxNativeMessagingManifestApplier : NativeMessagingManifestApplier() {
    private val firefoxNativeMessagingPath
        get() = Path(AppProperties.userDir, ".mozilla/native-messaging-hosts", "${AppInfo.packageName}.json")
    private val chromeNativeMessagingPath
        get() = Path(AppProperties.userDir, ".config/google-chrome/NativeMessagingHosts", "${AppInfo.packageName}.json")
    private val chromiumNativeMessagingPath
        get() = Path(AppProperties.userDir, ".config/chromium/NativeMessagingHosts", "${AppInfo.packageName}.json")

    override fun updateManifests(manifests: NativeMessagingManifests) {
        listOf(
            firefoxNativeMessagingPath,
            chromeNativeMessagingPath,
            chromiumNativeMessagingPath
        ).forEach { it.createParentDirectories() }

        firefoxNativeMessagingPath.writeText(serialize(manifests.firefoxNativeMessagingManifest))
        val chromeManifestString = serialize(manifests.chromeNativeMessagingManifest)
        chromeNativeMessagingPath.writeText(chromeManifestString)
        chromiumNativeMessagingPath.writeText(chromeManifestString)
    }

    override fun removeManifests() {
        firefoxNativeMessagingPath.deleteIfExists()
        chromeNativeMessagingPath.deleteIfExists()
        chromiumNativeMessagingPath.deleteIfExists()
    }
}

class NoOpNativeMessagingApplier : NativeMessagingManifestApplier() {
    override fun updateManifests(manifests: NativeMessagingManifests) {
        //no-op
    }

    override fun removeManifests() {
        //no-op
    }
}