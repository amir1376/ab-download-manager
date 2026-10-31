package com.abdownloadmanager.integration


data class IntegrationSettings(
    val port: Int,
    val apiKeyEnforced: Boolean,
    val apiKey: String?,
)
