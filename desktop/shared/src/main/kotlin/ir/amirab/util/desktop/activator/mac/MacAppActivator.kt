package ir.amirab.util.desktop.activator.mac

import com.sun.jna.Native
import ir.amirab.util.desktop.PlatformAppActivator

object MacAppActivator : PlatformAppActivator {
    private val foundation by lazy {
        runCatching { Native.load("objc", Foundation::class.java) }.getOrNull()
    }

    override fun active() {
        val requiredFoundation = foundation ?: return
        runCatching {
            val nsAppClass = requiredFoundation.objc_getClass("NSApplication")
            val sharedAppSel = requiredFoundation.sel_registerName("sharedApplication")
            val activateSel = requiredFoundation.sel_registerName("activateIgnoringOtherApps:")

            val nsApp = requiredFoundation.objc_msgSend(nsAppClass, sharedAppSel)
            requiredFoundation.objc_msgSend(nsApp, activateSel, true)
        }
    }
}