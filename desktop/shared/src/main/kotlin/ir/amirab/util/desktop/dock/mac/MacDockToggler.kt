package ir.amirab.util.desktop.dock.mac

import ir.amirab.util.desktop.PlatformDockToggler
import ir.amirab.util.desktop.utils.mac.FoundationLibrary

class MacDockToggler : PlatformDockToggler {

    private val foundation = FoundationLibrary.INSTANCE

    private val isAvailable = foundation != null

    private val nsAppClass by lazy { foundation!!.objc_getClass("NSApplication") }
    private val sharedAppSel by lazy { foundation!!.sel_registerName("sharedApplication") }
    private val setPolicySel by lazy { foundation!!.sel_registerName("setActivationPolicy:") }

    private val nsRunningAppClass by lazy { foundation!!.objc_getClass("NSRunningApplication") }
    private val currentAppSel by lazy { foundation!!.sel_registerName("currentApplication") }
    private val activateSel by lazy { foundation!!.sel_registerName("activateWithOptions:") }

    private val NSApplicationActivationPolicyRegular = 0
    private val NSApplicationActivationPolicyAccessory = 1
    private val NSApplicationActivateIgnoringOtherApps = 1

    override fun show() {
        if (isAvailable) {
            setPolicy(NSApplicationActivationPolicyRegular)
        }
    }

    override fun hide() {
        if (isAvailable) {
            hideAndKeepFocus()
        }
    }

    private fun hideAndKeepFocus() {
        val nsApp = foundation!!.objc_msgSend(nsAppClass, sharedAppSel)
        val nsRunningApp = foundation.objc_msgSend(nsRunningAppClass, currentAppSel)

        foundation.objc_msgSend(nsApp, setPolicySel, NSApplicationActivationPolicyAccessory)
        foundation.objc_msgSend(nsRunningApp, activateSel, NSApplicationActivateIgnoringOtherApps)
    }

    private fun setPolicy(policy: Int) {
        val nsApp = foundation!!.objc_msgSend(nsAppClass, sharedAppSel)
        foundation.objc_msgSend(nsApp, setPolicySel, policy)
    }
}