package ir.amirab.util.desktop.keepawake

import com.sun.jna.platform.win32.Kernel32

class WindowsKeepAwake : KeepAwake {
    override fun keepAwake() {
        runCatching {
            Kernel32.INSTANCE.SetThreadExecutionState(
                Kernel32.ES_CONTINUOUS or Kernel32.ES_SYSTEM_REQUIRED
            )
        }
    }

    override fun allowSleep() {
        runCatching {
            Kernel32.INSTANCE.SetThreadExecutionState(
                Kernel32.ES_CONTINUOUS
            )
        }
    }
}
