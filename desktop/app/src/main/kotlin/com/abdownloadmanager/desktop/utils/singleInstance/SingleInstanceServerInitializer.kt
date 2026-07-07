package com.abdownloadmanager.desktop.utils.singleInstance

import ir.amirab.util.guardedEntry
import org.koin.core.component.KoinComponent

/**
 * should be booted after the app/di is fully loaded and functional
 */
object SingleInstanceServerInitializer : KoinComponent {
    val booted = guardedEntry()
    fun boot() {
        booted.action {
            // do nothing
        }
    }
}
