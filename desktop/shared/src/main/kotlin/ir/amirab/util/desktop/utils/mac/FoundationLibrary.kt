package ir.amirab.util.desktop.utils.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

internal interface FoundationLibrary : Library {
    fun objc_getClass(name: String): Pointer
    fun sel_registerName(name: String): Pointer

    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, b: Boolean): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, i: Int): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, l: Long): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, p: Pointer): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer, o: Any): Pointer


    companion object {
        val INSTANT by lazy {
            runCatching { Native.load("objc", FoundationLibrary::class.java) }.getOrNull()
        }
    }
}