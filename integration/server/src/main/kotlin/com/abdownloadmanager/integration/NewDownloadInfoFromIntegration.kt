package com.abdownloadmanager.integration
import kotlinx.serialization.Serializable

@Serializable
data class NewDownloadInfoFromIntegration(
    val link: String,
    val headers: Map<String, String>?=null,
    val downloadPage:String?=null,
)