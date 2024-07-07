package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.Commands.GET_INTEGRATION_PORT
import com.abdownloadmanager.desktop.Commands.IS_READY
import com.abdownloadmanager.desktop.Commands.SHOW_USER_THAT_APP_IS_RUNNING
import com.abdownloadmanager.desktop.utils.IntegrationPortBroadcaster
import com.abdownloadmanager.desktop.utils.singleInstance.Command
import com.abdownloadmanager.desktop.utils.singleInstance.MutableSingleInstanceServerHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
object Commands {
    const val IS_READY = "IS_READY"
    const val SHOW_USER_THAT_APP_IS_RUNNING = "SHOW_USER_THAT_APP_IS_RUNNING"
    const val GET_INTEGRATION_PORT = "GET_INTEGRATION_PORT"
    val isReady = Command<Boolean>("isReady")
    val showUserThatAppIsRunning = Command<Unit>("showUserThatAppIsRunning")
    val getIntegrationPort = Command<Int>("getIntegrationPort")
}
object SingleInstanceServerInitializer:KoinComponent {
    private val appComponent by inject<AppComponent> ()
    fun boot(mutableHandler: MutableSingleInstanceServerHandler){
        mutableHandler.add(Commands.showUserThatAppIsRunning){
            kotlin.runCatching { appComponent.openHome() }
        }
        mutableHandler.add(Commands.getIntegrationPort){
            IntegrationPortBroadcaster
                .getIntegrationPort().let { it?:-1 }
        }
        mutableHandler.add(Commands.isReady){
            appComponent.isReady()
        }
    }
}