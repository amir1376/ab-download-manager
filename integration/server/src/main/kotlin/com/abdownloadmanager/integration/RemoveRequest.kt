package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class RemoveRequest(
    val ids: List<Long>,
    val keepFile: Boolean = true,
)