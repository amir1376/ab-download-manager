package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.utils.IntegrationPortBroadcaster
import com.abdownloadmanager.desktop.utils.singleInstance.Command
import com.abdownloadmanager.desktop.utils.singleInstance.MutableSingleInstanceServerHandler
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
object Commands {
    val isReady = Command<Boolean>("isReady")
    val showUserThatAppIsRunning = Command<Unit>("showUserThatAppIsRunning")
    val getIntegrationPort = Command<Int>("getIntegrationPort")
    val exit = Command<Unit>("exit")
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
        mutableHandler.add(Commands.exit) {
            runBlocking {
                appComponent.exitApp()
            }
        }
    }
}