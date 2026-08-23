package com.abdownloadmanager.android.storage

import androidx.datastore.core.DataStore
import arrow.optics.Lens
import com.abdownloadmanager.shared.storage.appsettings.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.appsettings.AppSettingsModel
import com.abdownloadmanager.shared.storage.appsettings.*
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import com.abdownloadmanager.shared.util.ui.theme.DEFAULT_UI_SCALE


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
    settings: DataStore<AppSettingsModel>,
) : BaseAppSettingsStorage,
    ConfigBaseSettingsByJson<AppSettingsModel>(settings) {
    override val theme = from(AppSettingsModel.theme)
    override val defaultDarkTheme = from(AppSettingsModel.defaultDarkTheme)
    override val defaultLightTheme = from(AppSettingsModel.defaultLightTheme)

    override val selectedLanguage = from(languageLens)
    override val font = from(fontLens)
    override val uiScale = from(uiScaleLens)
    override val showIconLabels = from(AppSettingsModel.showIconLabels)
    override val useRelativeDateTime = from(AppSettingsModel.useRelativeDateTime)
    override val threadCount = from(AppSettingsModel.threadCount)
    override val maxConcurrentDownloads = from(AppSettingsModel.maxConcurrentDownloads)
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
    override val generalNotificationSound = from(AppSettingsModel.generalNotificationSound)
    override val errorNotificationSound = from(AppSettingsModel.errorNotificationSound)
    override val successNotificationSound = from(AppSettingsModel.successNotificationSound)
    override val defaultDownloadFolder = from(AppSettingsModel.defaultDownloadFolder)
    override val apiEnabled = from(AppSettingsModel.apiEnabled)
    override val apiPort = from(AppSettingsModel.apiPort)
    override val apiAuthEnabled = from(AppSettingsModel.apiAuthEnabled)
    override val apiAuthKey = from(AppSettingsModel.apiAuthKey)
    override val trackDeletedFilesOnDisk = from(AppSettingsModel.trackDeletedFilesOnDisk)
    override val deletePartialFileOnDownloadCancellation =
        from(AppSettingsModel.deletePartialFileOnDownloadCancellation)
    override val sizeUnit = from(AppSettingsModel.sizeUnit)
    override val speedUnit = from(AppSettingsModel.speedUnit)
    override val ignoreSSLCertificates = from(AppSettingsModel.ignoreSSLCertificates)
    override val useCategoryByDefault = from(AppSettingsModel.useCategoryByDefault)
    override val userAgent = from(AppSettingsModel.userAgent)

    val browserIconInLauncher = from(AppSettingsModel.browserIconInLauncher)
}
