package com.abdownloadmanager.shared.util

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual object ClipboardUtil {
    private val clipboard get() = Toolkit.getDefaultToolkit().systemClipboard

    actual fun copy(text: String) {
        runCatching {
            clipboard.setContents(StringSelection(text), null)
        }
    }

    actual fun read(): String? {
        return runCatching {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        }.getOrNull()
    }
}
