package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import ir.amirab.downloader.connection.UserAgentProvider

class UserAgentProviderFromSettings(
    private val appSettingsStorage: AppSettingsStorage
) : UserAgentProvider {
    override fun getUserAgent(): String? {
        return appSettingsStorage.userAgent.value.takeIf { it.isNotBlank() }
    }
}
