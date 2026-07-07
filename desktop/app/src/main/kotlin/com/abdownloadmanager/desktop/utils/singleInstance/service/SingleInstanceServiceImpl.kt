package com.abdownloadmanager.desktop.utils.singleInstance.service

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.utils.IntegrationPortBroadcaster
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceServerInitializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SingleInstanceServiceImpl : ISingleInstanceService, KoinComponent {
    private val appComponent by inject<AppComponent>()

    suspend fun awaitReady() {
        SingleInstanceServerInitializer.booted.awaitDone()
    }

    override suspend fun getIntegrationPort(): Int {
        awaitReady()
        return IntegrationPortBroadcaster
            .getIntegrationPort()
    }

    override suspend fun isReady(): Boolean {
        awaitReady()
        return appComponent.isReady()
    }

    override suspend fun showUserThatAppIsRunning() {
        awaitReady()
        appComponent.openHome()
    }

    override suspend fun exit() {
        awaitReady()
        appComponent.exitApp()
    }
}
