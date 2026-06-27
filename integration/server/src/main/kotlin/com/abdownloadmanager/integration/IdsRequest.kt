package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class IdsRequest(
    val ids: List<Long>,
)