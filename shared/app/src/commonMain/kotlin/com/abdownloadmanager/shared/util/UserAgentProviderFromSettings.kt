package com.abdownloadmanager.shared.util

import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import ir.amirab.downloader.connection.UserAgentProvider

class UserAgentProviderFromSettings(
    private val appSettingsStorage: BaseAppSettingsStorage
) : UserAgentProvider {
    override fun getUserAgent(): String? {
        return appSettingsStorage.userAgent.value.takeIf { it.isNotBlank() }
    }
}
