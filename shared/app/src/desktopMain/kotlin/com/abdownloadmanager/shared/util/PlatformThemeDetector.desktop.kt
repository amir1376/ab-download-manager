package com.abdownloadmanager.shared.util

import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import dev.nucleusframework.darkmodedetector.getPlatformDarkModeDetector
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.function.Consumer

actual typealias PlatformThemeDetector = DesktopSystemThemeDetector

class DesktopSystemThemeDetector : ISystemThemeDetector {
    override val isSupported = true
    private val detector by lazy { getPlatformDarkModeDetector() }
    private val isSystemDarkFlowByLibrary = callbackFlow<Boolean> {
        val listener = Consumer<Boolean> { isDark: Boolean ->
            trySend(isDark)
        }
        detector.registerListener(listener)
        awaitClose {
            detector.removeListener(listener)
        }
    }

    override fun isDark() = detector.isDark()
    override val systemThemeFlow = flow {
        if (!isSupported) {
            return@flow
        }
        emit(detector.isDark())
        emitAll(isSystemDarkFlowByLibrary)
    }
}
