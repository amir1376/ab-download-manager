package com.abdownloadmanager.shared.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.input.key.KeyEvent

abstract class ShortcutManager {
    private val shortcuts = mutableMapOf<PlatformKeyStroke, () -> Unit>()
    fun register(keyStroke: PlatformKeyStroke, action: () -> Unit) {
        shortcuts[keyStroke] = action
    }

    abstract fun stringToKeyStroke(keyStrokeString: String): PlatformKeyStroke
    abstract fun getKeyStrokeFromEvent(s: KeyEvent): PlatformKeyStroke?

    infix fun String.to(action: () -> Unit) {
        register(stringToKeyStroke(this), action)
    }

    fun executeShortcut(
        keyStroke: PlatformKeyStroke,
    ): Boolean {
        val action = shortcuts[keyStroke] ?: return false
        runCatching {
            action()
        }
        return true
    }

    fun getShortCutOf(action: () -> Unit): PlatformKeyStroke? {
        return shortcuts.firstNotNullOfOrNull {
            if (it.value == action) {
                it.key
            } else null
        }
    }

    fun handle(event: KeyEvent): Boolean {
        val keyStroke = getKeyStrokeFromEvent(event) ?: return false
        executeShortcut(keyStroke)
        return true
    }
}

val LocalShortCutManager = compositionLocalOf {
    null as ShortcutManager?
}