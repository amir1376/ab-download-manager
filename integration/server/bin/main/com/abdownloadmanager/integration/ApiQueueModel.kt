package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class ApiQueueModel(
        val id: Long,
        val name: String,
)
