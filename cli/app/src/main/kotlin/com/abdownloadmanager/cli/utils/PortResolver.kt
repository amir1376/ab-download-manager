package com.abdownloadmanager.cli.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * Resolves the desktop app's integration port from appSettings.json.
 *
 * Uses the same path resolution as the desktop app's DefinedPaths.
 * Location: ~/.abdm/config/appSettings.json
 * Key: "browserIntegrationPort"
 *
 * Per maintainer: "always use the appSettings.json value, no retries needed"
 */
object PortResolver {

    fun readIntegrationPort(): Int? {
        val configFile = getSettingsFile()
        if (!configFile.exists()) return null
        return try {
            val text = configFile.readText().trim()
            if (text.isEmpty()) return null
            val json = Json { ignoreUnknownKeys = true }
            val root = json.decodeFromString<Map<String, JsonElement>>(text)
            root["browserIntegrationPort"]?.jsonPrimitive?.content?.toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun getSettingsFile(): File {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".abdm/config/appSettings.json")
    }
}