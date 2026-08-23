package com.abdownloadmanager.shared.storage.appsettings

actual object PlatformDefaultSettings : DefaultAppSettings() {
    override val useSparseFileAllocation: Boolean get() = false

    val browserIconInLauncher: Boolean get() = false
}