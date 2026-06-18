package ir.amirab.util.desktop.keepawake

import com.sun.jna.platform.win32.Kernel32
import kotlin.concurrent.thread

class WindowsKeepAwake : KeepAwake {
    /**
     * 1.keepAwake -> 2.cancellation require to be called in single thread
     */
    @Volatile
    private var thread: Thread? = null

    @Synchronized
    override fun keepAwake() {
        if (thread != null) {
            // already active
            return
        }
        thread = thread(
            name = "WindowsKeepAwake",
            isDaemon = true,
        ) {
            try {
                // keep the system awake!
                Kernel32.INSTANCE.SetThreadExecutionState(
                    Kernel32.ES_CONTINUOUS or Kernel32.ES_SYSTEM_REQUIRED
                )
                Thread.sleep(Long.MAX_VALUE)
            } catch (_: InterruptedException) {
                // we expect this!
            } catch (e: Exception) {
                // it shouldn't happen, but we don't throw any exception here!
                e.printStackTrace()
            } finally {
                // thread interrupted! now we can allow system to go sleep!
                runCatching {
                    Kernel32.INSTANCE.SetThreadExecutionState(
                        Kernel32.ES_CONTINUOUS
                    )
                }
                thread = null
            }
        }
    }

    @Synchronized
    override fun allowSleep() {
        thread?.let {
            it.interrupt()
            it.join()
        }
    }
}
