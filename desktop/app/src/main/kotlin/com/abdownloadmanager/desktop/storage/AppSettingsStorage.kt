package com.abdownloadmanager.desktop.storage

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import arrow.optics.optics
import com.abdownloadmanager.shared.utils.ConfigBaseSettingsByMapConfig
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.*
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import java.io.File

@optics([arrow.optics.OpticsTarget.LENS])
@Serializable
data class AppSettingsModel(
    val theme: String = "dark",
    val language: String? = null,
    val uiScale: Float? = null,
    val mergeTopBarWithTitleBar: Boolean = false,
    val showIconLabels: Boolean = true,
    val useSystemTray: Boolean = true,
    val threadCount: Int = 8,
    val maxDownloadRetryCount: Int = 0,
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
    val defaultDownloadFolder: String = File(System.getProperty("user.home"))
        .resolve("Downloads/ABDM")
        .canonicalFile.absolutePath,
    val browserIntegrationEnabled: Boolean = true,
    val browserIntegrationPort: Int = 15151,
    val trackDeletedFilesOnDisk: Boolean = false,
    val useBitsForSpeed: Boolean = false,
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
            val language = stringKeyOf("language")
            val uiScale = floatKeyOf("uiScale")
            val mergeTopBarWithTitleBar = booleanKeyOf("mergeTopBarWithTitleBar")
            val showIconLabels = booleanKeyOf("showIconLabels")
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
            val useBitsForSpeed = booleanKeyOf("useBitsForSpeed")
            val ignoreSSLCertificates = booleanKeyOf("ignoreSSLCertificates")
            val useCategoryByDefault = booleanKeyOf("useCategoryByDefault")
            val userAgent = stringKeyOf("userAgent")
        }


        override fun get(source: MapConfig): AppSettingsModel {
            val default by lazy { AppSettingsModel.default }
            return AppSettingsModel(
                theme = source.get(Keys.theme) ?: default.theme,
                language = source.get(Keys.language) ?: default.language,
                uiScale = source.get(Keys.uiScale) ?: default.uiScale,
                mergeTopBarWithTitleBar = source.get(Keys.mergeTopBarWithTitleBar) ?: default.mergeTopBarWithTitleBar,
                showIconLabels = source.get(Keys.showIconLabels) ?: default.showIconLabels,
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
                useBitsForSpeed = source.get(Keys.useBitsForSpeed) ?: default.useBitsForSpeed,
                ignoreSSLCertificates = source.get(Keys.ignoreSSLCertificates) ?: default.ignoreSSLCertificates,
                useCategoryByDefault = source.get(Keys.useCategoryByDefault) ?: default.useCategoryByDefault,
                userAgent = source.get(Keys.userAgent) ?: default.userAgent,
            )
        }

        override fun set(source: MapConfig, focus: AppSettingsModel): MapConfig {
            return source.apply {
                put(Keys.theme, focus.theme)
                putNullable(Keys.language, focus.language)
                putNullable(Keys.uiScale, focus.uiScale)
                put(Keys.mergeTopBarWithTitleBar, focus.mergeTopBarWithTitleBar)
                put(Keys.showIconLabels, focus.showIconLabels)
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
                put(Keys.useBitsForSpeed, focus.useBitsForSpeed)
                put(Keys.ignoreSSLCertificates, focus.ignoreSSLCertificates)
                put(Keys.useCategoryByDefault, focus.useCategoryByDefault)
                put(Keys.userAgent, focus.userAgent)
            }
        }
    }
}

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
    override val selectedLanguage = from(languageLens)
    val uiScale = from(uiScaleLens)
    val mergeTopBarWithTitleBar = from(AppSettingsModel.mergeTopBarWithTitleBar)
    val showIconLabels = from(AppSettingsModel.showIconLabels)
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
    val useBitsForSpeed = from(AppSettingsModel.useBitsForSpeed)
    val ignoreSSLCertificates = from(AppSettingsModel.ignoreSSLCertificates)
    val useCategoryByDefault = from(AppSettingsModel.useCategoryByDefault)
    val userAgent = from(AppSettingsModel.userAgent)
}
