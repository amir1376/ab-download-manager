package com.abdownloadmanager.desktop.nativemessaging

import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.AppProperties
import com.abdownloadmanager.desktop.utils.isAppInstalled
import ir.amirab.util.createParentDirectories
import ir.amirab.util.deleteIfExists
import ir.amirab.util.desktop.WindowsRegistry
import ir.amirab.util.platform.Platform
import ir.amirab.util.writeText
import kotlinx.serialization.json.Json
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.core.component.KoinComponent
import kotlin.io.path.Path

abstract class NativeMessagingManifestApplier(val json: Json) : KoinComponent {
    protected inline fun <reified T : Any> serialize(data: T): String {
        return json.encodeToString(data)
    }

    protected inline fun <reified T : Any> deserialize(string: String): T {
        return json.decodeFromString(string)
    }

    abstract fun updateManifests(manifests: NativeMessagingManifests)
    abstract fun removeManifests()

    protected fun writeManifestContent(
        path: Path,
        manifestContent: String
    ) {
        path.createParentDirectories()
        path.writeText(manifestContent)
    }

    companion object {
        fun getForCurrentPlatform(json: Json): NativeMessagingManifestApplier {
            if (!AppInfo.isAppInstalled()) {
                return NoOpNativeMessagingApplier(json)
            }
            return when (AppInfo.platform) {
                Platform.Desktop.Linux -> LinuxNativeMessagingManifestApplier(json)
                Platform.Desktop.MacOS -> MacosNativeMessagingManifestApplier(json)
                Platform.Desktop.Windows -> WindowsNativeMessagingManifestApplier(json)
                Platform.Android -> error("there is no native messaging for android so this code should never used in android")
            }
        }
    }
}

class WindowsNativeMessagingManifestApplier(json: Json) : NativeMessagingManifestApplier(json) {
    private val baseNativeMessagingDir get() = AppInfo.definedPaths.configDir / "native_messaging"
    private val firefoxManifestFile get() = baseNativeMessagingDir / "firefox" / "${AppInfo.packageName}.json"
    private val chromeManifestFile get() = baseNativeMessagingDir / "chrome" / "${AppInfo.packageName}.json"
    private val firefoxRegistryPath get() = "HKCU\\SOFTWARE\\Mozilla\\NativeMessagingHosts\\${AppInfo.packageName}"
    private val chromeRegistryPath get() = "HKCU\\SOFTWARE\\Google\\Chrome\\NativeMessagingHosts\\${AppInfo.packageName}"

    override fun updateManifests(
        manifests: NativeMessagingManifests
    ) {
        writeManifestContent(firefoxManifestFile, serialize(manifests.firefoxNativeMessagingManifest))
        WindowsRegistry.setValueInRegistry(
            path = firefoxRegistryPath,
            key = null,
            value = firefoxManifestFile.toString()
        )

        writeManifestContent(chromeManifestFile, serialize(manifests.chromeNativeMessagingManifest))
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

class MacosNativeMessagingManifestApplier(
    json: Json
) : NativeMessagingManifestApplier(json) {

    private val nativeMessagingFileName = "${AppInfo.packageName}.json"

    // firefox and chromium paths are the same just app name is different
    private fun createNativeMessagingPath(appSupportDir: String) =
        Path(
            AppProperties.userDir,
            "Library/Application Support",
            appSupportDir,
            "NativeMessagingHosts",
            nativeMessagingFileName
        ).toOkioPath()

    private val firefoxNativeMessagingPaths = listOf(
        createNativeMessagingPath("Mozilla")
    )

    private val chromiumBasedNativeMessagingPaths = listOf(
        createNativeMessagingPath("Google/Chrome"),
        createNativeMessagingPath("Chromium"),
    )

    override fun updateManifests(manifests: NativeMessagingManifests) {
        val firefoxManifest = serialize(manifests.firefoxNativeMessagingManifest)
        firefoxNativeMessagingPaths.forEach { path ->
            writeManifestContent(path, firefoxManifest)
        }

        val chromiumManifest = serialize(manifests.chromeNativeMessagingManifest)
        chromiumBasedNativeMessagingPaths.forEach { path ->
            writeManifestContent(path, chromiumManifest)
        }
    }

    override fun removeManifests() {
        firefoxNativeMessagingPaths.forEach { path ->
            path.deleteIfExists()
        }

        chromiumBasedNativeMessagingPaths.forEach { path ->
            path.deleteIfExists()
        }
    }
}

class LinuxNativeMessagingManifestApplier(
    json: Json
) : NativeMessagingManifestApplier(json) {

    private val nativeMessagingFileName = "${AppInfo.packageName}.json"

    private val firefoxNativeMessagingPaths
        get() = listOf(
            Path(
                AppProperties.userDir,
                ".mozilla/native-messaging-hosts",
                nativeMessagingFileName
            ).toOkioPath()
        )


    private fun createChromiumNativeMessagingPath(browserConfigDir: String) = Path(
        AppProperties.userDir,
        ".config",
        browserConfigDir,
        "NativeMessagingHosts",
        nativeMessagingFileName
    ).toOkioPath()

    private val chromiumBasedNativeMessagingPaths
        get() = listOf(
            createChromiumNativeMessagingPath("google-chrome"),
            createChromiumNativeMessagingPath("chromium"),
            createChromiumNativeMessagingPath("BraveSoftware/Brave-Browser"),
            createChromiumNativeMessagingPath("microsoft-edge"),
            createChromiumNativeMessagingPath("vivaldi"),
            createChromiumNativeMessagingPath("opera")
        )

    override fun updateManifests(manifests: NativeMessagingManifests) {
        val firefoxManifest = serialize(manifests.firefoxNativeMessagingManifest)
        firefoxNativeMessagingPaths.forEach { path ->
            writeManifestContent(path, firefoxManifest)
        }

        val chromiumManifest = serialize(manifests.chromeNativeMessagingManifest)
        chromiumBasedNativeMessagingPaths.forEach { path ->
            writeManifestContent(path, chromiumManifest)
        }
    }

    override fun removeManifests() {
        firefoxNativeMessagingPaths.forEach { path ->
            path.deleteIfExists()
        }

        chromiumBasedNativeMessagingPaths.forEach { path ->
            path.deleteIfExists()
        }
    }
}

class NoOpNativeMessagingApplier(
    json: Json
) : NativeMessagingManifestApplier(
    json,
) {
    override fun updateManifests(manifests: NativeMessagingManifests) {
        //no-op
    }

    override fun removeManifests() {
        //no-op
    }
}
