package com.abdownloadmanager.desktop.utils.singleInstance

import ir.amirab.util.guardedEntry
import org.koin.core.component.KoinComponent

/**
 * boot should be called after the main app/di is fully loaded and functional
 */
object SingleInstanceInitialized : KoinComponent {
    private val booted = guardedEntry()

    fun boot() {
        booted.action {
            // do nothing
        }
    }

    suspend fun awaitDone() {
        booted.awaitDone()
    }

    fun isDone(): Boolean {
        return booted.isDone()
    }
}
