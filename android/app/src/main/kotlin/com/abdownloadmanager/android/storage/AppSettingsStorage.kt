package com.abdownloadmanager.android.storage

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import arrow.optics.optics
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.IAppSettingsModel
import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import com.abdownloadmanager.shared.util.downloadlocation.PlatformDownloadLocationProvider
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByMapConfig
import com.abdownloadmanager.shared.util.ui.theme.DEFAULT_UI_SCALE
import ir.amirab.util.config.*
import ir.amirab.util.enumValueOrNull
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@optics([arrow.optics.OpticsTarget.LENS])
@Serializable
data class AppSettingsModel(
    override val theme: String = "dark",
    override val defaultDarkTheme: String = "dark",
    override val defaultLightTheme: String = "light",
    override val language: String? = null,
    override val font: String? = null,
    override val uiScale: Float? = null,
    override val showIconLabels: Boolean = true,
    override val useRelativeDateTime: Boolean = true,
    override val threadCount: Int = 8,
    override val maxDownloadRetryCount: Int = 3,
    override val dynamicPartCreation: Boolean = true,
    override val useServerLastModifiedTime: Boolean = false,
    override val appendExtensionToIncompleteDownloads: Boolean = false,
    override val useSparseFileAllocation: Boolean = true,
    override val useAverageSpeed: Boolean = true,
    override val showDownloadProgressDialog: Boolean = true,
    override val showDownloadCompletionDialog: Boolean = true,
    override val speedLimit: Long = 0,
    override val autoStartOnBoot: Boolean = true,
    override val notificationSound: Boolean = true,
    override val defaultDownloadFolder: String = PlatformDownloadLocationProvider
        .instance.getDownloadLocation()
        .resolve("ABDM")
        .canonicalFile.absolutePath,
    override val browserIntegrationEnabled: Boolean = true,
    override val browserIntegrationPort: Int = 15151,
    override val trackDeletedFilesOnDisk: Boolean = false,
    override val deletePartialFileOnDownloadCancellation: Boolean = false,
    override val sizeUnit: SupportedSizeUnits = SupportedSizeUnits.BinaryBytes,
    override val speedUnit: SupportedSizeUnits = SupportedSizeUnits.BinaryBytes,
    override val ignoreSSLCertificates: Boolean = false,
    override val useCategoryByDefault: Boolean = true,
    override val userAgent: String = "",
) : IAppSettingsModel {
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
                showIconLabels = source.get(Keys.showIconLabels) ?: default.showIconLabels,
                useRelativeDateTime = source.get(Keys.useRelativeDateTime) ?: default.useRelativeDateTime,
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
                put(Keys.showIconLabels, focus.showIconLabels)
                put(Keys.useRelativeDateTime, focus.useRelativeDateTime)
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

// use null for default scale!
private val uiScaleLens: Lens<AppSettingsModel, Float>
    get() = Lens(
        get = {
            it.uiScale ?: DEFAULT_UI_SCALE
        },
        set = { s, f ->
            s.copy(uiScale = f.takeIf { it != DEFAULT_UI_SCALE })
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
) : BaseAppSettingsStorage,
    ConfigBaseSettingsByMapConfig<AppSettingsModel>(settings, AppSettingsModel.ConfigLens) {
    override val theme = from(AppSettingsModel.theme)
    override val defaultDarkTheme = from(AppSettingsModel.defaultDarkTheme)
    override val defaultLightTheme = from(AppSettingsModel.defaultLightTheme)

    override val selectedLanguage = from(languageLens)
    override val font = from(fontLens)
    override val uiScale = from(uiScaleLens)
    override val showIconLabels = from(AppSettingsModel.showIconLabels)
    override val useRelativeDateTime = from(AppSettingsModel.useRelativeDateTime)
    override val threadCount = from(AppSettingsModel.threadCount)
    override val dynamicPartCreation = from(AppSettingsModel.dynamicPartCreation)
    override val useServerLastModifiedTime = from(AppSettingsModel.useServerLastModifiedTime)
    override val appendExtensionToIncompleteDownloads = from(AppSettingsModel.appendExtensionToIncompleteDownloads)
    override val useSparseFileAllocation = from(AppSettingsModel.useSparseFileAllocation)
    override val useAverageSpeed = from(AppSettingsModel.useAverageSpeed)
    override val maxDownloadRetryCount = from(AppSettingsModel.maxDownloadRetryCount)
    override val showDownloadProgressDialog = from(AppSettingsModel.showDownloadProgressDialog)
    override val showDownloadCompletionDialog = from(AppSettingsModel.showDownloadCompletionDialog)
    override val speedLimit = from(AppSettingsModel.speedLimit)
    override val autoStartOnBoot = from(AppSettingsModel.autoStartOnBoot)
    override val notificationSound = from(AppSettingsModel.notificationSound)
    override val defaultDownloadFolder = from(AppSettingsModel.defaultDownloadFolder)
    override val browserIntegrationEnabled = from(AppSettingsModel.browserIntegrationEnabled)
    override val browserIntegrationPort = from(AppSettingsModel.browserIntegrationPort)
    override val trackDeletedFilesOnDisk = from(AppSettingsModel.trackDeletedFilesOnDisk)
    override val deletePartialFileOnDownloadCancellation =
        from(AppSettingsModel.deletePartialFileOnDownloadCancellation)
    override val sizeUnit = from(AppSettingsModel.sizeUnit)
    override val speedUnit = from(AppSettingsModel.speedUnit)
    override val ignoreSSLCertificates = from(AppSettingsModel.ignoreSSLCertificates)
    override val useCategoryByDefault = from(AppSettingsModel.useCategoryByDefault)
    override val userAgent = from(AppSettingsModel.userAgent)
}
