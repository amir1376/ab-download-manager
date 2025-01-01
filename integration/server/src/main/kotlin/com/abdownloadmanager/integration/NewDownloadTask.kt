package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class NewDownloadTask(
        val downloadSource: NewDownloadInfoFromIntegration,
        var folder: String? = null,
        var name: String? = null,
        var queueId: Long? = null,
)
