package ir.amirab.util.desktop.dock.mac

import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import ir.amirab.util.desktop.PlatformDockToggler

object MacDockToggler : PlatformDockToggler {
    private val objc: NativeLibrary = NativeLibrary.getInstance("objc")

    private val getClass = objc.getFunction("objc_getClass")
    private val getSel = objc.getFunction("sel_registerName")
    private val msgSend = objc.getFunction("objc_msgSend")

    private val nsAppClass: Pointer = getClass.invokePointer(arrayOf("NSApplication"))
    private val sharedAppSel: Pointer = getSel.invokePointer(arrayOf("sharedApplication"))
    private val setPolicySel: Pointer = getSel.invokePointer(arrayOf("setActivationPolicy:"))

    private val nsRunningAppClass: Pointer = getClass.invokePointer(arrayOf("NSRunningApplication"))
    private val currentAppSel: Pointer = getSel.invokePointer(arrayOf("currentApplication"))
    private val activateSel: Pointer = getSel.invokePointer(arrayOf("activateWithOptions:"))

    private val NSApplicationActivationPolicyRegular = 0
    private val NSApplicationActivationPolicyAccessory = 1
    private val NSApplicationActivateIgnoringOtherApps = 1

    override fun show() = setPolicy(NSApplicationActivationPolicyRegular)

    override fun hide() = hideAndKeepFocus()

    private fun hideAndKeepFocus() {
        val nsApp = msgSend.invokePointer(arrayOf(nsAppClass, sharedAppSel))
        val nsRunningApp = msgSend.invokePointer(arrayOf(nsRunningAppClass, currentAppSel))

        msgSend.invokeVoid(arrayOf(nsApp, setPolicySel, NSApplicationActivationPolicyAccessory))
        msgSend.invokeVoid(
            arrayOf(
                nsRunningApp,
                activateSel,
                NSApplicationActivateIgnoringOtherApps
            )
        )
    }

    private fun setPolicy(policy: Int) {
        val nsApp = msgSend.invokePointer(arrayOf(nsAppClass, sharedAppSel))
        msgSend.invokeVoid(arrayOf(nsApp, setPolicySel, policy))
    }
}