package com.abdownloadmanager.shared.util

import org.jetbrains.skiko.ClipboardManager

actual object ClipboardUtil {
    private val clipboardManager = ClipboardManager()

    actual fun copy(text: String) {
        runCatching {
            clipboardManager.setText(text.toString())
        }
    }

    actual fun read(): String? {
        return runCatching {
            clipboardManager.getText()
        }.getOrNull()
    }
}
