package com.abdownloadmanager.shared.util.downloaderror

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class DownloadErrorReason(
    val title: String,
    val description: String,
    val suggestion: String,
    val throwableName: String,
    val throwableMessage: String?,
)
