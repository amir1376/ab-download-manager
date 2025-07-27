package ir.amirab.util.desktop.keepawake

interface KeepAwake {
    /**
     * Prevents the system from going to sleep.
     */
    fun keepAwake()

    /**
     * Allows the system to go to sleep again.
     */
    fun allowSleep()

    class NoOpKeepAwake : KeepAwake {
        override fun keepAwake() {
            // No operation, does nothing
        }

        override fun allowSleep() {
            // No operation, does nothing
        }
    }
}
