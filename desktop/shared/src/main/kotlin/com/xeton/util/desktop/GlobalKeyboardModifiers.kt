package com.xeton.util.desktop

import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.platform.WindowInfo
import com.xeton.util.platform.Platform
import com.xeton.util.platform.isMac

fun isCtrlPressed(windowInfo: WindowInfo): Boolean {
    val keyboardModifiers = windowInfo.keyboardModifiers
    return if (Platform.isMac()) {
        keyboardModifiers.isMetaPressed
    } else {
        keyboardModifiers.isCtrlPressed
    }
}

fun isShiftPressed(windowInfo: WindowInfo): Boolean {
    return windowInfo.keyboardModifiers.isShiftPressed
}
