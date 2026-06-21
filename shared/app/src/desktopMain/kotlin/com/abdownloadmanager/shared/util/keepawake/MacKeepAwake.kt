package com.abdownloadmanager.shared.util.keepawake

class MacKeepAwake : KeepAwake {
    var process: Process? = null

    @Synchronized
    override fun keepAwake() {
        process?.destroy()
        process = runCatching {
            ProcessBuilder("caffeinate", "-s")
                .redirectErrorStream(true)
                .start()
        }.getOrElse { null }
    }

    override fun allowSleep() {
        runCatching {
            process?.destroy()
        }
    }
}
