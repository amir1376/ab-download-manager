package com.abdownloadmanager.integration.client

import ir.amirab.util.config.MapConfig
import ir.amirab.util.config.intKeyOf
import ir.amirab.util.config.loadFromJson
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Resolves the desktop app's integration port from appSettings.json.
 *
 * Uses the shared MapConfig + ConfigToJson infrastructure (same as
 * AppSettingsModel.ConfigLens) rather than raw JSON parsing.
 *
 * File location: matches AppInfo.dataDir / config/appSettings.json
 * Key: "browserIntegrationPort" (defined in AppSettingsModel.Keys)
 */
object PortResolver {

    private val json = Json { ignoreUnknownKeys = true }

    fun readIntegrationPort(): Int? {
        val configFile = getSettingsFile()
        if (!configFile.exists()) return null
        return try {
            val text = configFile.readText().trim()
            if (text.isEmpty()) return null
            val mapConfig = MapConfig()
            val jsonObject = json.decodeFromString<kotlinx.serialization.json.JsonObject>(text)
            mapConfig.loadFromJson(jsonObject)
            mapConfig.get(intKeyOf("browserIntegrationPort"))
        } catch (_: Exception) {
            null
        }
    }

    private fun getSettingsFile(): File {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".abdm/config/appSettings.json")
    }
}