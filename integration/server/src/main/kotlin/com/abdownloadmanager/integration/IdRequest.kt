package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class IdRequest(
    val id: Long,
)