package com.abdownloadmanager.shared.util

import com.abdownloadmanager.shared.BuildConfig
import com.abdownloadmanager.shared.util.BaseConstants
import com.abdownloadmanager.shared.util.BrowserIntegrationModel
import com.abdownloadmanager.shared.util.BrowserType

object SharedConstants : BaseConstants {
    override val appName: String = BuildConfig.APP_NAME
    override val appDisplayName: String = BuildConfig.APP_DISPLAY_NAME
    override val packageName: String = BuildConfig.PACKAGE_NAME
    override val dataDirName: String = BuildConfig.DATA_DIR_NAME
    override val projectWebsite: String = BuildConfig.PROJECT_WEBSITE
    override val projectTranslations: String = BuildConfig.PROJECT_TRANSLATIONS
    override val projectSourceCode: String = BuildConfig.PROJECT_SOURCE_CODE
    override val donateLink: String = BuildConfig.DONATE_LINK
    override val projectGithubOwner: String = BuildConfig.PROJECT_GITHUB_OWNER
    override val projectGithubRepo: String = BuildConfig.PROJECT_GITHUB_REPO
    override val browserIntegrations: List<BrowserIntegrationModel> = listOf(
        BrowserIntegrationModel(
            BrowserType.Chrome, BuildConfig.INTEGRATION_CHROME_LINK
        ),
        BrowserIntegrationModel(
            BrowserType.Firefox, BuildConfig.INTEGRATION_FIREFOX_LINK
        )
    )
    override val telegramChannelUrl: String = BuildConfig.TELEGRAM_CHANNEL
    override val telegramGroupUrl: String = BuildConfig.TELEGRAM_GROUP
}
