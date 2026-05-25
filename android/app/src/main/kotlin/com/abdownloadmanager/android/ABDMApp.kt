package com.abdownloadmanager.android

import android.app.Application
import co.touchlab.kermit.Severity
import com.abdownloadmanager.android.di.Di
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.android.util.AndroidGlobalExceptionHandler
import com.abdownloadmanager.android.util.AppInfo
import com.abdownloadmanager.android.util.ApplicationBackgroundTracker
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.util.appinfo.PreviousVersion
import ir.amirab.util.logger.AppLogger
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ABDMApp : Application(), KoinComponent {
    val TAG_NAME = ABDMApp::class.simpleName!!
    val appManager: ABDMAppManager by inject()
    val appRepository: BaseAppRepository by inject()
    val previousVersion: PreviousVersion by inject()
    val scope: CoroutineScope by inject()
    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)
        AppLogger.init(
            writeToConsole = true,
            logFilePath = null,
            minSeverity = Severity.Verbose,
        )
        Di.boot(this)
        ApplicationBackgroundTracker.startTracking(this)
        appRepository.boot()
        previousVersion.boot()
        Thread.setDefaultUncaughtExceptionHandler(
            AndroidGlobalExceptionHandler(
                this,
                Thread.getDefaultUncaughtExceptionHandler(),
            )
        )
        appManager.boot()
    }
}
