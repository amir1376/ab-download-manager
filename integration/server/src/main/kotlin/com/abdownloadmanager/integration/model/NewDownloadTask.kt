package com.abdownloadmanager.integration.model

import kotlinx.serialization.Serializable

@Serializable
data class NewDownloadTask(
    val downloadSource: IDownloadCredentialsFromIntegration,
    val folder: String? = null,
    val name: String? = null,
    val queueId: Long? = null,
    val categoryId: Long? = null,
    val startDownload: Boolean = false,
    val startQueue: Boolean = false,
)
