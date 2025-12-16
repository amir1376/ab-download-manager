package com.abdownloadmanager.shared.pages.adddownload

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface AddDownloadConfig {
    val id: String
    val importOptions: ImportOptions

    @Serializable
    data class SingleAddConfig(
        val newDownload: AddDownloadCredentialsInUiProps,
        override val importOptions: ImportOptions = ImportOptions(),
        override val id: String = UUID.randomUUID().toString(),
    ) : AddDownloadConfig

    @Serializable
    data class MultipleAddConfig(
        val newDownloads: List<AddDownloadCredentialsInUiProps> = emptyList(),
        override val importOptions: ImportOptions = ImportOptions(),
        override val id: String = UUID.randomUUID().toString(),
    ) : AddDownloadConfig

}

