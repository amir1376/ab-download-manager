package com.abdownloadmanager.desktop.utils

import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.input.key.KeyEvent
import com.abdownloadmanager.shared.utils.PlatformKeyStroke
import com.abdownloadmanager.shared.utils.ShortcutManager
import java.awt.Toolkit
import java.awt.event.InputEvent
import javax.swing.KeyStroke

class DesktopShortcutManager : ShortcutManager() {
    override fun stringToKeyStroke(keyStrokeString: String): PlatformKeyStroke {
        return KeyStroke
            .getKeyStroke(keyStrokeString)
            .asPlatformKeyStroke()
    }

    override fun getKeyStrokeFromEvent(s: KeyEvent): PlatformKeyStroke? {
        val awtEvent = s.awtEventOrNull ?: return null
        return runCatching {
            KeyStroke.getKeyStrokeForEvent(awtEvent)
        }.getOrNull()?.asPlatformKeyStroke()
    }
}

data class DesktopKeyStroke(
    val awtKeyStroke: KeyStroke,
) : PlatformKeyStroke {
    override fun getModifiers(): List<String> {
        return KeyUtil.getModifiers(awtKeyStroke.modifiers)
    }

    override fun getKeyText(): String {
        return KeyUtil.getKeyText(awtKeyStroke.keyCode)
    }
}

fun KeyStroke.asPlatformKeyStroke() = DesktopKeyStroke(this)

object KeyUtil {
    fun getKeyText(keyCode: Int): String {
        return java.awt.event.KeyEvent.getKeyText(keyCode)
    }

    fun getModifiers(modifiers: Int): List<String> {
        return buildList<String> {
            if (modifiers and InputEvent.META_DOWN_MASK != 0) {
                add(Toolkit.getProperty("AWT.meta", "Meta"))
            }
            if (modifiers and InputEvent.CTRL_DOWN_MASK != 0) {
                add(Toolkit.getProperty("AWT.control", "Ctrl"))
            }
            if (modifiers and InputEvent.ALT_DOWN_MASK != 0) {
                add(Toolkit.getProperty("AWT.alt", "Alt"))
            }
            if (modifiers and InputEvent.SHIFT_DOWN_MASK != 0) {
                add(Toolkit.getProperty("AWT.shift", "Shift"))
            }
            if (modifiers and InputEvent.ALT_GRAPH_DOWN_MASK != 0) {
                add(Toolkit.getProperty("AWT.altGraph", "Alt Graph"))
            }
        }
    }
}