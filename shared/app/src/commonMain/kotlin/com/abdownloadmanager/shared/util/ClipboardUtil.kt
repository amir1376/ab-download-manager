package com.abdownloadmanager.shared.util


expect object ClipboardUtil {
    fun read(): String?
    fun copy(text: String)
}
