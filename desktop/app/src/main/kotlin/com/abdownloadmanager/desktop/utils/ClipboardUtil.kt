package com.abdownloadmanager.desktop.utils

import org.jetbrains.skiko.ClipboardManager
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard

import java.awt.datatransfer.StringSelection




object ClipboardUtil {
    private val clipboardManager = ClipboardManager()

    fun copy(text:String){
        runCatching {
            clipboardManager.setText(text)
        }
//        .onFailure {
//            it.printStackTrace()
//        }
    }
    fun read(): String? {
        return runCatching {
            clipboardManager.getText()
        }.getOrNull()
    }
}
