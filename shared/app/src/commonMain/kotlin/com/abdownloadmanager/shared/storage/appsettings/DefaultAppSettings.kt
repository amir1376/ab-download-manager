package com.abdownloadmanager.shared.storage.appsettings

import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import com.abdownloadmanager.shared.util.ApiKeyUtil
import com.abdownloadmanager.shared.util.downloadlocation.PlatformDownloadLocationProvider

expect object PlatformDefaultSettings : DefaultAppSettings

abstract class DefaultAppSettings {
    open val theme: String get() = "dark"
    open val defaultDarkTheme: String get() = "dark"
    open val defaultLightTheme: String get() = "light"
    open val language: String? get() = null
    open val font: String? get() = null
    open val uiScale: Float? get() = null
    open val showIconLabels: Boolean get() = true
    open val useRelativeDateTime: Boolean get() = true
    open val threadCount: Int get() = 8
    open val maxConcurrentDownloads: Int get() = 3
    open val maxDownloadRetryCount: Int get() = 3
    open val dynamicPartCreation: Boolean get() = true
    open val useServerLastModifiedTime: Boolean get() = false
    open val appendExtensionToIncompleteDownloads: Boolean get() = false
    abstract val useSparseFileAllocation: Boolean
    open val useAverageSpeed: Boolean get() = true
    open val showDownloadProgressDialog: Boolean get() = true
    open val showDownloadCompletionDialog: Boolean get() = true
    open val speedLimit: Long get() = 0
    open val autoStartOnBoot: Boolean get() = true
    open val notificationSound: Boolean get() = true
    open val generalNotificationSound: String get() = ""
    open val successNotificationSound: String get() = ""
    open val errorNotificationSound: String get() = ""
    open val defaultDownloadFolder: String
        get() = PlatformDownloadLocationProvider.instance.getDownloadLocation()
            .resolve("ABDM").canonicalFile.absolutePath
    open val apiEnabled: Boolean get() = true
    open val apiPort: Int get() = 15151
    open val apiAuthEnabled: Boolean get() = false
    open val apiAuthKey: String get() = ApiKeyUtil.generateKey()
    open val trackDeletedFilesOnDisk: Boolean get() = false
    open val deletePartialFileOnDownloadCancellation: Boolean get() = false
    open val sizeUnit: SupportedSizeUnits get() = SupportedSizeUnits.BinaryBytes
    open val speedUnit: SupportedSizeUnits get() = SupportedSizeUnits.BinaryBytes
    open val ignoreSSLCertificates: Boolean get() = false
    open val useCategoryByDefault: Boolean get() = true
    open val userAgent: String get() = ""
}