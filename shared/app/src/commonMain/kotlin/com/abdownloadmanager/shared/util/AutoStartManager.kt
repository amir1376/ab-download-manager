package com.abdownloadmanager.shared.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ir.amirab.util.startup.AbstractStartupManager

object AutoStartManager : KoinComponent {
    private val startManager by inject<AbstractStartupManager>()
    fun startOnBoot(boolean: Boolean) {
//        println("start Manager is ${startManager}")
        if (boolean) {
            startManager.install()
        } else {
            startManager.uninstall()
        }
    }
}
