package com.abdownloadmanager.shared.storage.appsettings

actual object PlatformDefaultSettings : DefaultAppSettings() {
    override val useSparseFileAllocation: Boolean get() = true

    val mergeTopBarWithTitleBar: Boolean get() = true
    val useNativeMenuBar: Boolean get() = false
    val useSystemTray: Boolean get() = true
}