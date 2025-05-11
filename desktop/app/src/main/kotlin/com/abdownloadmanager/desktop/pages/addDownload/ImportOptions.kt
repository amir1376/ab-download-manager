package com.abdownloadmanager.desktop.pages.addDownload

data class SilentImportOptions(
    val silentDownload: Boolean,
)

data class ImportOptions(
    val silentImport: SilentImportOptions? = null,
)
