package com.abdownloadmanager.desktop.actions

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.input.key.KeyEvent
import javax.swing.KeyStroke

class ShortcutManager {
    private val shortcuts = mutableMapOf<KeyStroke, () -> Unit>()
    fun register(keyStroke: KeyStroke, action: () -> Unit) {
        shortcuts[keyStroke] = action
    }

    infix fun String.to(action: () -> Unit) {
        register(KeyStroke.getKeyStroke(this), action)
    }

    fun executeShortcut(
        keyStroke: KeyStroke,
    ): Boolean {
        val action = shortcuts[keyStroke] ?: return false
        runCatching {
            action()
        }
        return true
    }

    fun getShortCutOf(action: () -> Unit): KeyStroke? {
        return shortcuts.firstNotNullOfOrNull {
            if (it.value==action){
                it.key
            }else null
        }
    }
}
val LocalShortCutManager = compositionLocalOf {
    null as ShortcutManager?
}
fun ShortcutManager.handle(event: KeyEvent): Boolean {
    val awtEvent = event.awtEventOrNull ?: return false
    val keyStroke = runCatching {
        KeyStroke.getKeyStrokeForEvent(awtEvent)
    }.getOrNull() ?: return false
    executeShortcut(keyStroke)
    return true
}