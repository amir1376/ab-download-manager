package ir.amirab.util.desktop.activator.mac

import com.sun.jna.Library
import com.sun.jna.Pointer

interface Foundation : Library {
    fun objc_getClass(name: String): Pointer
    fun sel_registerName(name: String): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, b: Boolean): Pointer
}