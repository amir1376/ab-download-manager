package com.abdownloadmanager.desktop.ui.theme

import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class SystemThemeDetector {
    val isSupported by lazy {
        OsThemeDetector.isSupported()
    }
    private val detector by lazy { OsThemeDetector.getDetector() }

    private val isSystemDarkFlowByLibrary = callbackFlow<Boolean> {
        val listener: (Boolean) -> Unit = { isDark: Boolean ->
            trySend(isDark)
        }
        detector.registerListener(listener)
        awaitClose {
            detector.removeListener(listener)
        }
    }
    val isDark = detector.isDark
    val systemThemeFlow = flow {
        if (!isSupported){
            return@flow
        }
        emit(detector.isDark)
        emitAll(isSystemDarkFlowByLibrary)
    }
}