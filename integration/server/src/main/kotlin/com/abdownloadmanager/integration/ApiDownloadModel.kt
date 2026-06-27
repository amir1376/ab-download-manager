package com.abdownloadmanager.integration

import kotlinx.serialization.Serializable

@Serializable
data class ApiDownloadModel(
    val id: Long,
    val name: String,
    val url: String,
    val folder: String,
    val status: String,
    val size: Long,
    val downloaded: Long = 0,
    val speed: Long = 0,
    val progress: Double = 0.0,
    val dateAdded: Long,
    val startTime: Long? = null,
    val completeTime: Long? = null,
    val connections: Int? = null,
    val speedLimit: Long = 0,
    val checksum: String? = null,
)