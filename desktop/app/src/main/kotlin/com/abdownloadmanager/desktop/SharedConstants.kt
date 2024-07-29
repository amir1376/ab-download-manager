package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.utils.BrowserIntegrationModel
import com.abdownloadmanager.desktop.utils.BrowserType

interface BaseConstants{
    val appName:String
    val packageName:String
    val projectWebsite:String
    val projectSourceCode:String
    val browserIntegrations:List<BrowserIntegrationModel>
    val telegramGroupUrl:String
    val telegramChannelUrl:String
}

object SharedConstants:BaseConstants{
    override val appName: String = BuildConfig.APP_NAME
    override val packageName: String = BuildConfig.PACKAGE_NAME
    override val projectWebsite: String= BuildConfig.PROJECT_WEBSITE
    override val projectSourceCode: String= BuildConfig.PROJECT_SOURCE_CODE
    override val browserIntegrations: List<BrowserIntegrationModel> = listOf(
        BrowserIntegrationModel(
            BrowserType.Chrome,BuildConfig.INTEGRATION_CHROME_LINK
        ),
        BrowserIntegrationModel(
            BrowserType.Firefox,BuildConfig.INTEGRATION_FIREFOX_LINK
        )
    )
    override val telegramChannelUrl: String = BuildConfig.TELEGRAM_CHANNEL
    override val telegramGroupUrl: String = BuildConfig.TELEGRAM_GROUP
}
