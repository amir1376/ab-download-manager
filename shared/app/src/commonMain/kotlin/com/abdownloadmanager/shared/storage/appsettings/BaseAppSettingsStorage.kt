package com.abdownloadmanager.shared.storage.appsettings

import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import com.abdownloadmanager.shared.ui.theme.ThemeSettingsStorage
import com.abdownloadmanager.shared.util.notification.INotificationSettingsStorage
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import kotlinx.coroutines.flow.MutableStateFlow


interface BaseAppSettingsStorage :
    LanguageStorage,
    ThemeSettingsStorage,
    INotificationSettingsStorage {
    override val theme: MutableStateFlow<String>
    override val defaultDarkTheme: MutableStateFlow<String>
    override val defaultLightTheme: MutableStateFlow<String>
    override val selectedLanguage: MutableStateFlow<String?>
    val font: MutableStateFlow<String?>
    val uiScale: MutableStateFlow<Float>
    val showIconLabels: MutableStateFlow<Boolean>
    val useRelativeDateTime: MutableStateFlow<Boolean>
    val threadCount: MutableStateFlow<Int>
    val maxConcurrentDownloads: MutableStateFlow<Int>
    val dynamicPartCreation: MutableStateFlow<Boolean>
    val useServerLastModifiedTime: MutableStateFlow<Boolean>
    val appendExtensionToIncompleteDownloads: MutableStateFlow<Boolean>
    val useSparseFileAllocation: MutableStateFlow<Boolean>
    val useAverageSpeed: MutableStateFlow<Boolean>
    val maxDownloadRetryCount: MutableStateFlow<Int>
    val showDownloadProgressDialog: MutableStateFlow<Boolean>
    val showDownloadCompletionDialog: MutableStateFlow<Boolean>
    val speedLimit: MutableStateFlow<Long>
    val autoStartOnBoot: MutableStateFlow<Boolean>
    override val notificationSound: MutableStateFlow<Boolean>
    override val generalNotificationSound: MutableStateFlow<String>
    override val errorNotificationSound: MutableStateFlow<String>
    override val successNotificationSound: MutableStateFlow<String>
    val defaultDownloadFolder: MutableStateFlow<String>
    val apiEnabled: MutableStateFlow<Boolean>
    val apiPort: MutableStateFlow<Int>
    val apiAuthEnabled: MutableStateFlow<Boolean>
    val apiAuthKey: MutableStateFlow<String>
    val trackDeletedFilesOnDisk: MutableStateFlow<Boolean>
    val deletePartialFileOnDownloadCancellation: MutableStateFlow<Boolean>
    val sizeUnit: MutableStateFlow<SupportedSizeUnits>
    val speedUnit: MutableStateFlow<SupportedSizeUnits>
    val ignoreSSLCertificates: MutableStateFlow<Boolean>
    val useCategoryByDefault: MutableStateFlow<Boolean>
    val userAgent: MutableStateFlow<String>
}
