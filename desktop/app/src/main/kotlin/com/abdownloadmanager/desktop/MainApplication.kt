package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.ui.Ui
import com.abdownloadmanager.desktop.utils.GlobalAppExceptionHandler
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessaging
import com.abdownloadmanager.desktop.utils.renderapi.CustomRenderApi
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceInitialized
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.appinfo.PreviousVersion
import com.abdownloadmanager.shared.util.keepawake.KeepAwakeManager
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Main gui application startup
 */

class MainApplication : AutoCloseable,
    KoinComponent {
    private val downloadSystem: DownloadSystem by inject()
    private val appRepository: AppRepository by inject()
    private val integration: Integration by inject()
    private val previousVersion: PreviousVersion by inject()
    private val keepAwakeManager: KeepAwakeManager by inject()
    private val customRenderApi: CustomRenderApi by inject()

    private val browserNativeMessaging: NativeMessaging by inject()
    fun start(
        appArguments: AppArguments,
        globalAppExceptionHandler: GlobalAppExceptionHandler,
    ) {
        try {
            runBlocking {
                //make sure to not get any dependency until boot the DI Container
                Di.boot()
                // it's better to organize these list of boot functions in a separate class

                // boot configs from the storage so download manager can use them on boot!
                customRenderApi.boot()
                appRepository.boot()
                integration.boot()
                downloadSystem.boot()
                previousVersion.boot()
                keepAwakeManager.boot()
                //waiting for compose kmp to add multi launcher to nativeDistributions,the PR is already exists but not merger
                //or maybe I should use a custom solution
                browserNativeMessaging.boot()
                Ui.boot(appArguments)
                // at this point the single instance is fully functional
                // we can signal ipc that we are ready to accept requests
                SingleInstanceInitialized.boot()
                // start the gui
                Ui.start(globalAppExceptionHandler)
            }
        } catch (e: Exception) {
            globalAppExceptionHandler.onProcessIsUseless()
            throw e
        }
    }

    override fun close() {
        //nothing yet!
    }
}
