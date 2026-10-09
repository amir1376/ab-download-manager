package com.abdownloadmanager.shared.util.systemusage

import com.sun.jna.Library
import com.sun.jna.Native

/**
 * JNA binding to macOS CoreGraphics framework.
 */
internal interface CoreGraphicsLibrary : Library {
    fun CGEventSourceSecondsSinceLastEventType(sourceStateId: Int, eventType: Int): Double

    companion object {
        val INSTANCE: CoreGraphicsLibrary? by lazy {
            runCatching {
                Native.load("CoreGraphics", CoreGraphicsLibrary::class.java)
            }.getOrNull()
        }
    }
}

/**
 * macOS implementation of [DesktopIdleTimeProvider].
 *
 * Primary method:
 * - Uses CoreGraphics `CGEventSourceSecondsSinceLastEventType` with `kCGEventSourceStateCombinedSessionState`.
 *   This is standard on macOS, lightweight, and does NOT require Accessibility (`AXIsProcessTrusted`)
 *   or Input Monitoring permissions.
 *
 * Fallback:
 * - Queries IOKit's `IOHIDSystem` via `ioreg -c IOHIDSystem` to read `HIDIdleTime` property.
 */
internal class MacIdleTimeProvider : DesktopIdleTimeProvider {
    companion object {
        // kCGEventSourceStateCombinedSessionState = 0
        private const val CG_EVENT_SOURCE_STATE_COMBINED_SESSION_STATE = 0

        // kCGAnyInputEventType = ~0 = -1
        private const val CG_ANY_INPUT_EVENT_TYPE = -1
    }

    override fun getIdleTimeMillis(): Long? {
        // 1. Try CoreGraphics
        val cg = CoreGraphicsLibrary.INSTANCE
        if (cg != null) {
            val seconds = runCatching {
                cg.CGEventSourceSecondsSinceLastEventType(
                    CG_EVENT_SOURCE_STATE_COMBINED_SESSION_STATE,
                    CG_ANY_INPUT_EVENT_TYPE,
                )
            }.getOrNull()

            if (seconds != null && seconds >= 0.0) {
                return (seconds * 1000.0).toLong()
            }
        }

        // 2. Fallback to IOKit via ioreg
        return getIdleTimeViaIoreg()
    }

    private fun getIdleTimeViaIoreg(): Long? {
        return runCatching {
            val process = ProcessBuilder("ioreg", "-c", "IOHIDSystem")
                .redirectErrorStream(true)
                .start()
            val text = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()

            val match = Regex(""""HIDIdleTime"\s*=\s*(\d+)""").find(text)
            val nano = match?.groupValues?.get(1)?.toLongOrNull()
            nano?.let { it / 1_000_000L }
        }.getOrNull()
    }
}
