package ir.amirab.util.desktop.activator.mac

import ir.amirab.util.desktop.PlatformAppActivator
import ir.amirab.util.desktop.utils.mac.FoundationLibrary

class MacAppActivator : PlatformAppActivator {
    override fun active() {
        val requiredFoundation = FoundationLibrary.INSTANT ?: return
        runCatching {
            val nsAppClass = requiredFoundation.objc_getClass("NSApplication")
            val sharedAppSel = requiredFoundation.sel_registerName("sharedApplication")
            val activateSel = requiredFoundation.sel_registerName("activateIgnoringOtherApps:")

            val nsApp = requiredFoundation.objc_msgSend(nsAppClass, sharedAppSel)
            requiredFoundation.objc_msgSend(nsApp, activateSel, true)
        }
    }
}