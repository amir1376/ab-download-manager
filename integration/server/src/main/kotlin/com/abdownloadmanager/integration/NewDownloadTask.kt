package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class NewDownloadTask(
    val downloadSource: DownloadCredentialsFromIntegration,
    var folder: String? = null,
    var name: String? = null,
    var queueId: Long? = null,
)
