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

@optics
@Serializable
data class AppSettingsModel(
    val theme: String = "dark",
    val language: String? = null,
    val uiScale: Float? = null,
    val mergeTopBarWithTitleBar: Boolean = false,
    val threadCount: Int = 8,
    val dynamicPartCreation: Boolean = true,
    val useServerLastModifiedTime: Boolean = false,
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
            val threadCount = intKeyOf("threadCount")
            val dynamicPartCreation = booleanKeyOf("dynamicPartCreation")
            val useServerLastModifiedTime = booleanKeyOf("useServerLastModifiedTime")
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
        }


        override fun get(source: MapConfig): AppSettingsModel {
            val default by lazy { AppSettingsModel.default }
            return AppSettingsModel(
                theme = source.get(Keys.theme) ?: default.theme,
                language = source.get(Keys.language) ?: default.language,
                uiScale = source.get(Keys.uiScale) ?: default.uiScale,
                mergeTopBarWithTitleBar = source.get(Keys.mergeTopBarWithTitleBar) ?: default.mergeTopBarWithTitleBar,
                threadCount = source.get(Keys.threadCount) ?: default.threadCount,
                dynamicPartCreation = source.get(Keys.dynamicPartCreation) ?: default.dynamicPartCreation,
                useServerLastModifiedTime = source.get(Keys.useServerLastModifiedTime)
                    ?: default.useServerLastModifiedTime,
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
            )
        }

        override fun set(source: MapConfig, focus: AppSettingsModel): MapConfig {
            return source.apply {
                put(Keys.theme, focus.theme)
                putNullable(Keys.language, focus.language)
                putNullable(Keys.uiScale, focus.uiScale)
                put(Keys.mergeTopBarWithTitleBar, focus.mergeTopBarWithTitleBar)
                put(Keys.threadCount, focus.threadCount)
                put(Keys.dynamicPartCreation, focus.dynamicPartCreation)
                put(Keys.useServerLastModifiedTime, focus.useServerLastModifiedTime)
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
    var theme = from(AppSettingsModel.theme)
    override val selectedLanguage = from(languageLens)
    var uiScale = from(uiScaleLens)
    var mergeTopBarWithTitleBar = from(AppSettingsModel.mergeTopBarWithTitleBar)
    val threadCount = from(AppSettingsModel.threadCount)
    val dynamicPartCreation = from(AppSettingsModel.dynamicPartCreation)
    val useServerLastModifiedTime = from(AppSettingsModel.useServerLastModifiedTime)
    val useSparseFileAllocation = from(AppSettingsModel.useSparseFileAllocation)
    val useAverageSpeed = from(AppSettingsModel.useAverageSpeed)
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
}
