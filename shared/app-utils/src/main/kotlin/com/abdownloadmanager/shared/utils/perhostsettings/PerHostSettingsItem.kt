package com.abdownloadmanager.shared.utils.perhostsettings

import kotlinx.serialization.Serializable

@Serializable
data class PerHostSettingsItem(
    val host: String,
    val username: String? = null,
    val password: String? = null,
    val userAgent: String? = null,
    val threadCount: Int? = null,
    val speedLimit: Long? = null,
)
