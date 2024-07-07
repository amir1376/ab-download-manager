package com.abdownloadmanager.desktop.utils

import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

object KeyUtil{
    fun getKeyText(keyCode:Int): String {
        return KeyEvent.getKeyText(keyCode)
    }
    fun getModifiers(modifiers:Int): List<String> {
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