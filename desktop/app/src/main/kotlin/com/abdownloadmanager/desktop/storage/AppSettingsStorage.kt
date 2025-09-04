package com.abdownloadmanager.desktop.storage

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import arrow.optics.optics
import com.abdownloadmanager.shared.utils.ConfigBaseSettingsByMapConfig
import com.abdownloadmanager.shared.utils.SystemDownloadLocationProvider
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.*
import ir.amirab.util.enumValueOrNull
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@optics([arrow.optics.OpticsTarget.LENS])
@Serializable
data class AppSettingsModel(
    val theme: String = "dark",
    val defaultDarkTheme: String = "dark",
    val defaultLightTheme: String = "light",
    val language: String? = null,
    val font: String? = null,
    val uiScale: Float? = null,
    val mergeTopBarWithTitleBar: Boolean = false,
    val useNativeMenuBar: Boolean = false,
    val showIconLabels: Boolean = true,
    val useRelativeDateTime: Boolean = true,
    val useSystemTray: Boolean = true,
    val threadCount: Int = 8,
    val maxDownloadRetryCount: Int = 3,
    val dynamicPartCreation: Boolean = true,
    val useServerLastModifiedTime: Boolean = false,
    val appendExtensionToIncompleteDownloads: Boolean = false,
    val useSparseFileAllocation: Boolean = true,
    val useAverageSpeed: Boolean = true,
    val showDownloadProgressDialog: Boolean = true,
    val showDownloadCompletionDialog: Boolean = true,
    val speedLimit: Long = 0,
    val autoStartOnBoot: Boolean = true,
    val notificationSound: Boolean = true,
    val defaultDownloadFolder: String = SystemDownloadLocationProvider
        .instance.getDownloadLocation()
        .resolve("ABDM")
        .canonicalFile.absolutePath,
    val browserIntegrationEnabled: Boolean = true,
    val browserIntegrationPort: Int = 15151,
    val trackDeletedFilesOnDisk: Boolean = false,
    val deletePartialFileOnDownloadCancellation: Boolean = false,
    val sizeUnit: SupportedSizeUnits = SupportedSizeUnits.BinaryBytes,
    val speedUnit: SupportedSizeUnits = SupportedSizeUnits.BinaryBytes,
    val ignoreSSLCertificates: Boolean = false,
    val useCategoryByDefault: Boolean = true,
    val userAgent: String = "",
) {
    companion object {
        val default: AppSettingsModel get() = AppSettingsModel()
    }

    object ConfigLens : Lens<MapConfig, AppSettingsModel>, KoinComponent {
        object Keys {
            val theme = stringKeyOf("theme")
            val defaultDarkTheme = stringKeyOf("defaultDarkTheme")
            val defaultLightTheme = stringKeyOf("defaultLightTheme")
            val language = stringKeyOf("language")
            val font = stringKeyOf("font")
            val uiScale = floatKeyOf("uiScale")
            val mergeTopBarWithTitleBar = booleanKeyOf("mergeTopBarWithTitleBar")
            val useNativeMenuBar = booleanKeyOf("useNativeMenuBar")
            val showIconLabels = booleanKeyOf("showIconLabels")
            val useRelativeDateTime = booleanKeyOf("useRelativeDateTime")
            val useSystemTray = booleanKeyOf("useSystemTray")
            val threadCount = intKeyOf("threadCount")
            val maxDownloadRetryCount = intKeyOf("maxDownloadRetryCount")
            val dynamicPartCreation = booleanKeyOf("dynamicPartCreation")
            val useServerLastModifiedTime = booleanKeyOf("useServerLastModifiedTime")
            val appendExtensionToIncompleteDownloads = booleanKeyOf("appendExtensionToIncompleteDownloads")
            val useSparseFileAllocation = booleanKeyOf("useSparseFileAllocation")
            val useAverageSpeed = booleanKeyOf("useAverageSpeed")
            val showDownloadProgressDialog = booleanKeyOf("showDownloadProgressDialog")
            val showDownloadCompletionDialog = booleanKeyOf("showDownloadCompletionDialog")
            val speedLimit = longKeyOf("speedLimit")
            val autoStartOnBoot = booleanKeyOf("autoStartOnBoot")
            val notificationSound = booleanKeyOf("notificationSound")
            val defaultDownloadFolder = stringKeyOf("defaultDownloadFolder")
            val browserIntegrationEnabled = booleanKeyOf("browserIntegrationEnabled")
            val browserIntegrationPort = intKeyOf("browserIntegrationPort")
            val trackDeletedFilesOnDisk = booleanKeyOf("trackDeletedFilesOnDisk")
            val deletePartialFileOnDownloadCancellation = booleanKeyOf("deletePartialFileOnDownloadCancellation")
            val sizeUnit = stringKeyOf("sizeUnit")
            val speedUnit = stringKeyOf("speedUnit")
            val ignoreSSLCertificates = booleanKeyOf("ignoreSSLCertificates")
            val useCategoryByDefault = booleanKeyOf("useCategoryByDefault")
            val userAgent = stringKeyOf("userAgent")
        }


        override fun get(source: MapConfig): AppSettingsModel {
            val default by lazy { AppSettingsModel.default }
            // for nullable types we don't get default value
            return AppSettingsModel(
                theme = source.get(Keys.theme) ?: default.theme,
                defaultDarkTheme = source.get(Keys.defaultDarkTheme) ?: default.defaultDarkTheme,
                defaultLightTheme = source.get(Keys.defaultLightTheme) ?: default.defaultLightTheme,
                language = source.get(Keys.language),
                font = source.get(Keys.font),
                uiScale = source.get(Keys.uiScale),
                mergeTopBarWithTitleBar = source.get(Keys.mergeTopBarWithTitleBar) ?: default.mergeTopBarWithTitleBar,
                useNativeMenuBar = source.get(Keys.useNativeMenuBar) ?: default.useNativeMenuBar,
                showIconLabels = source.get(Keys.showIconLabels) ?: default.showIconLabels,
                useRelativeDateTime = source.get(Keys.useRelativeDateTime) ?: default.useRelativeDateTime,
                useSystemTray = source.get(Keys.useSystemTray) ?: default.useSystemTray,
                threadCount = source.get(Keys.threadCount) ?: default.threadCount,
                maxDownloadRetryCount = source.get(Keys.maxDownloadRetryCount) ?: default.maxDownloadRetryCount,
                dynamicPartCreation = source.get(Keys.dynamicPartCreation) ?: default.dynamicPartCreation,
                useServerLastModifiedTime = source.get(Keys.useServerLastModifiedTime)
                    ?: default.useServerLastModifiedTime,
                appendExtensionToIncompleteDownloads = source.get(Keys.appendExtensionToIncompleteDownloads)
                    ?: default.appendExtensionToIncompleteDownloads,
                useSparseFileAllocation = source.get(Keys.useSparseFileAllocation) ?: default.useSparseFileAllocation,
                useAverageSpeed = source.get(Keys.useAverageSpeed) ?: default.useAverageSpeed,
                showDownloadProgressDialog = source.get(Keys.showDownloadProgressDialog)
                    ?: default.showDownloadProgressDialog,
                showDownloadCompletionDialog = source.get(Keys.showDownloadCompletionDialog)
                    ?: default.showDownloadCompletionDialog,
                speedLimit = source.get(Keys.speedLimit) ?: default.speedLimit,
                autoStartOnBoot = source.get(Keys.autoStartOnBoot) ?: default.autoStartOnBoot,
                notificationSound = source.get(Keys.notificationSound) ?: default.notificationSound,
                defaultDownloadFolder = source.get(Keys.defaultDownloadFolder) ?: default.defaultDownloadFolder,
                browserIntegrationEnabled = source.get(Keys.browserIntegrationEnabled)
                    ?: default.browserIntegrationEnabled,
                browserIntegrationPort = source.get(Keys.browserIntegrationPort) ?: default.browserIntegrationPort,
                trackDeletedFilesOnDisk = source.get(Keys.trackDeletedFilesOnDisk) ?: default.trackDeletedFilesOnDisk,
                deletePartialFileOnDownloadCancellation = source.get(Keys.deletePartialFileOnDownloadCancellation)
                    ?: default.deletePartialFileOnDownloadCancellation,
                sizeUnit = source.get(Keys.sizeUnit)?.enumValueOrNull<SupportedSizeUnits>() ?: default.sizeUnit,
                speedUnit = source.get(Keys.speedUnit)?.enumValueOrNull<SupportedSizeUnits>() ?: default.speedUnit,
                ignoreSSLCertificates = source.get(Keys.ignoreSSLCertificates) ?: default.ignoreSSLCertificates,
                useCategoryByDefault = source.get(Keys.useCategoryByDefault) ?: default.useCategoryByDefault,
                userAgent = source.get(Keys.userAgent) ?: default.userAgent,
            )
        }

        override fun set(source: MapConfig, focus: AppSettingsModel): MapConfig {
            return source.apply {
                put(Keys.theme, focus.theme)
                put(Keys.defaultDarkTheme, focus.defaultDarkTheme)
                put(Keys.defaultLightTheme, focus.defaultLightTheme)
                putNullable(Keys.language, focus.language)
                putNullable(Keys.font, focus.font)
                putNullable(Keys.uiScale, focus.uiScale)
                put(Keys.mergeTopBarWithTitleBar, focus.mergeTopBarWithTitleBar)
                put(Keys.useNativeMenuBar, focus.useNativeMenuBar)
                put(Keys.showIconLabels, focus.showIconLabels)
                put(Keys.useRelativeDateTime, focus.useRelativeDateTime)
                put(Keys.useSystemTray, focus.useSystemTray)
                put(Keys.threadCount, focus.threadCount)
                put(Keys.maxDownloadRetryCount, focus.maxDownloadRetryCount)
                put(Keys.dynamicPartCreation, focus.dynamicPartCreation)
                put(Keys.useServerLastModifiedTime, focus.useServerLastModifiedTime)
                put(Keys.appendExtensionToIncompleteDownloads, focus.appendExtensionToIncompleteDownloads)
                put(Keys.useSparseFileAllocation, focus.useSparseFileAllocation)
                put(Keys.useAverageSpeed, focus.useAverageSpeed)
                put(Keys.showDownloadProgressDialog, focus.showDownloadProgressDialog)
                put(Keys.showDownloadCompletionDialog, focus.showDownloadCompletionDialog)
                put(Keys.speedLimit, focus.speedLimit)
                put(Keys.autoStartOnBoot, focus.autoStartOnBoot)
                put(Keys.notificationSound, focus.notificationSound)
                put(Keys.defaultDownloadFolder, focus.defaultDownloadFolder)
                put(Keys.browserIntegrationEnabled, focus.browserIntegrationEnabled)
                put(Keys.browserIntegrationPort, focus.browserIntegrationPort)
                put(Keys.trackDeletedFilesOnDisk, focus.trackDeletedFilesOnDisk)
                put(Keys.deletePartialFileOnDownloadCancellation, focus.deletePartialFileOnDownloadCancellation)
                put(Keys.sizeUnit, focus.sizeUnit.name)
                put(Keys.speedUnit, focus.speedUnit.name)
                put(Keys.ignoreSSLCertificates, focus.ignoreSSLCertificates)
                put(Keys.useCategoryByDefault, focus.useCategoryByDefault)
                put(Keys.userAgent, focus.userAgent)
            }
        }
    }
}

private val fontLens: Lens<AppSettingsModel, String?>
    get() = Lens(
        get = {
            it.font
        },
        set = { s, f ->
            s.copy(font = f)
        }
    )
private val uiScaleLens: Lens<AppSettingsModel, Float?>
    get() = Lens(
        get = {
            it.uiScale
        },
        set = { s, f ->
            s.copy(uiScale = f)
        }
    )
private val languageLens: Lens<AppSettingsModel, String?>
    get() = Lens(
        get = {
            it.language
        },
        set = { s, f ->
            s.copy(language = f)
        }
    )

class AppSettingsStorage(
    settings: DataStore<MapConfig>,
) :
    ConfigBaseSettingsByMapConfig<AppSettingsModel>(settings, AppSettingsModel.ConfigLens),
    LanguageStorage {
    val theme = from(AppSettingsModel.theme)
    val defaultDarkTheme = from(AppSettingsModel.defaultDarkTheme)
    val defaultLightTheme = from(AppSettingsModel.defaultLightTheme)

    override val selectedLanguage = from(languageLens)
    val font = from(fontLens)
    val uiScale = from(uiScaleLens)
    val mergeTopBarWithTitleBar = from(AppSettingsModel.mergeTopBarWithTitleBar)
    val useNativeMenuBar = from(AppSettingsModel.useNativeMenuBar)
    val showIconLabels = from(AppSettingsModel.showIconLabels)
    val useRelativeDateTime = from(AppSettingsModel.useRelativeDateTime)
    val useSystemTray = from(AppSettingsModel.useSystemTray)
    val threadCount = from(AppSettingsModel.threadCount)
    val dynamicPartCreation = from(AppSettingsModel.dynamicPartCreation)
    val useServerLastModifiedTime = from(AppSettingsModel.useServerLastModifiedTime)
    val appendExtensionToIncompleteDownloads = from(AppSettingsModel.appendExtensionToIncompleteDownloads)
    val useSparseFileAllocation = from(AppSettingsModel.useSparseFileAllocation)
    val useAverageSpeed = from(AppSettingsModel.useAverageSpeed)
    val maxDownloadRetryCount = from(AppSettingsModel.maxDownloadRetryCount)
    val showDownloadProgressDialog = from(AppSettingsModel.showDownloadProgressDialog)
    val showDownloadCompletionDialog = from(AppSettingsModel.showDownloadCompletionDialog)
    val speedLimit = from(AppSettingsModel.speedLimit)
    val autoStartOnBoot = from(AppSettingsModel.autoStartOnBoot)
    val notificationSound = from(AppSettingsModel.notificationSound)
    val defaultDownloadFolder = from(AppSettingsModel.defaultDownloadFolder)
    val browserIntegrationEnabled = from(AppSettingsModel.browserIntegrationEnabled)
    val browserIntegrationPort = from(AppSettingsModel.browserIntegrationPort)
    val trackDeletedFilesOnDisk = from(AppSettingsModel.trackDeletedFilesOnDisk)
    val deletePartialFileOnDownloadCancellation = from(AppSettingsModel.deletePartialFileOnDownloadCancellation)
    val sizeUnit = from(AppSettingsModel.sizeUnit)
    val speedUnit = from(AppSettingsModel.speedUnit)
    val ignoreSSLCertificates = from(AppSettingsModel.ignoreSSLCertificates)
    val useCategoryByDefault = from(AppSettingsModel.useCategoryByDefault)
    val userAgent = from(AppSettingsModel.userAgent)
}
