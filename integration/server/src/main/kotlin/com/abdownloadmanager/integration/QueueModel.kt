package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class QueueModel(
        val id: Long,
        val name: String,
)
