package com.abdownloadmanager.shared.storage

import com.abdownloadmanager.shared.ui.theme.ThemeSettingsStorage
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import kotlinx.coroutines.flow.MutableStateFlow

interface IAppSettingsModel {
    val theme: String
    val defaultDarkTheme: String
    val defaultLightTheme: String
    val language: String?
    val font: String?
    val uiScale: Float?
    val showIconLabels: Boolean
    val useRelativeDateTime: Boolean
    val threadCount: Int
    val maxDownloadRetryCount: Int
    val dynamicPartCreation: Boolean
    val useServerLastModifiedTime: Boolean
    val appendExtensionToIncompleteDownloads: Boolean
    val useSparseFileAllocation: Boolean
    val useAverageSpeed: Boolean
    val showDownloadProgressDialog: Boolean
    val showDownloadCompletionDialog: Boolean
    val speedLimit: Long
    val autoStartOnBoot: Boolean
    val notificationSound: Boolean
    val defaultDownloadFolder: String
    val browserIntegrationEnabled: Boolean
    val browserIntegrationPort: Int
    val trackDeletedFilesOnDisk: Boolean
    val deletePartialFileOnDownloadCancellation: Boolean
    val sizeUnit: SupportedSizeUnits
    val speedUnit: SupportedSizeUnits
    val ignoreSSLCertificates: Boolean
    val useCategoryByDefault: Boolean
    val userAgent: String
}


interface BaseAppSettingsStorage :
    LanguageStorage,
    ThemeSettingsStorage {
    override val theme: MutableStateFlow<String>
    override val defaultDarkTheme: MutableStateFlow<String>
    override val defaultLightTheme: MutableStateFlow<String>
    override val selectedLanguage: MutableStateFlow<String?>
    val font: MutableStateFlow<String?>
    val uiScale: MutableStateFlow<Float>
    val showIconLabels: MutableStateFlow<Boolean>
    val useRelativeDateTime: MutableStateFlow<Boolean>
    val threadCount: MutableStateFlow<Int>
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
    val notificationSound: MutableStateFlow<Boolean>
    val defaultDownloadFolder: MutableStateFlow<String>
    val browserIntegrationEnabled: MutableStateFlow<Boolean>
    val browserIntegrationPort: MutableStateFlow<Int>
    val trackDeletedFilesOnDisk: MutableStateFlow<Boolean>
    val deletePartialFileOnDownloadCancellation: MutableStateFlow<Boolean>
    val sizeUnit: MutableStateFlow<SupportedSizeUnits>
    val speedUnit: MutableStateFlow<SupportedSizeUnits>
    val ignoreSSLCertificates: MutableStateFlow<Boolean>
    val useCategoryByDefault: MutableStateFlow<Boolean>
    val userAgent: MutableStateFlow<String>
}
