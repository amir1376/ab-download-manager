package com.abdownloadmanager.shared.pages.adddownload

import kotlinx.serialization.Serializable

@Serializable
data class SilentImportOptions(
    val silentDownload: Boolean,
)

@Serializable
data class ImportOptions(
    val silentImport: SilentImportOptions? = null,
    /**
     * true when the dialog is opened because an external app/browser integration asked for a download.
     */
    val externalRequest: Boolean = false,
)
