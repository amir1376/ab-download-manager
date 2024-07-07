package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.BuildConfig

interface BaseConstants{
    val appName:String
    val packageName:String
    val projectWebsite:String
    val projectSourceCode:String
}

object SharedConstants:BaseConstants{
    override val appName: String = BuildConfig.APP_NAME
    override val packageName: String = BuildConfig.PACKAGE_NAME
    override val projectWebsite: String= BuildConfig.PROJECT_WEBSITE
    override val projectSourceCode: String= BuildConfig.PROJECT_SOURCE_CODE
}
