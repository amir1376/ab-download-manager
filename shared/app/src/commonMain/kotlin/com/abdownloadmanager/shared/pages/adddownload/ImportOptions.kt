package com.abdownloadmanager.shared.pages.adddownload

import kotlinx.serialization.Serializable

@Serializable
data class SilentImportOptions(
    val silentDownload: Boolean,
)

@Serializable
data class ImportOptions(
    val silentImport: SilentImportOptions? = null,
)
