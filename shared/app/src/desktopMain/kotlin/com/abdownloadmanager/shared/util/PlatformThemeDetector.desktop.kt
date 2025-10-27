package com.abdownloadmanager.shared.util

import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import com.jthemedetecor.OsThemeDetector
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

actual typealias PlatformThemeDetector = DesktopSystemThemeDetector

class DesktopSystemThemeDetector : ISystemThemeDetector {
    override val isSupported by lazy {
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

    override fun isDark() = detector.isDark
    override val systemThemeFlow = flow {
        if (!isSupported) {
            return@flow
        }
        emit(detector.isDark)
        emitAll(isSystemDarkFlowByLibrary)
    }
}
