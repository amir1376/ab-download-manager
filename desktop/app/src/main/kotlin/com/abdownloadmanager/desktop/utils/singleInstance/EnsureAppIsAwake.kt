package com.abdownloadmanager.desktop.utils.singleInstance

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.utils.AppInfo
import ir.amirab.util.execAndWait
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object EnsureAppIsAwake {
    suspend fun awakeTheApp(
        timeout: Long = 10_000
    ) {
        if (awaitToBeReady()) {
            return
        }

        val executable = requireNotNull(AppInfo.exeFile) {
            "No executable file detected"
        }
        val result = execAndWait(
            arrayOf(
                executable,
                AppArguments.Args.START_IF_NOT_STARTED
            ),
            timeout,
        )

        if (!result) {
            error("Can't awake the app (process return non-zero code)")
        }
        if (!awaitToBeReady()) {
            error("Can't awake the app (its not ready)")
        }
    }
}

object StartIfNotStartedCommand {
    suspend fun startAndWaitForRunIfNotRunning(
        howMuchWait: Long = 10_000,
        initialDelay: Long = 0,
        eachTimeDelay: Long = 500L,
    ) {
        val deadLine = System.currentTimeMillis() + howMuchWait
        if (initialDelay > 0) {
            delay(initialDelay.milliseconds)
        }
        var firstLoop = true
        while (true) {
            val isReady: Boolean = awaitToBeReady()
//          println("isReady: $isReady")
            if (isReady) {
                return
            }
            if (firstLoop) {
                startAppInAnotherProcess()
//              println("send start signal")
            }
            if (System.currentTimeMillis() >= deadLine) {
//            println("dead line reached")
                //deadline reached exiting now
                error("Timed out waiting for application startup.")
            }
            delay(eachTimeDelay.milliseconds)
            firstLoop = false
        }
    }

    private fun startAppInAnotherProcess() {
        val exeFile = requireNotNull(AppInfo.exeFile)
        ProcessBuilder(
            exeFile,
            AppArguments.Args.BACKGROUND
        ).start()
    }
}

/**
 * tries to connect to single instance
 * - in case the app isn't running it quickly throws an exception
 * - otherwise waits until it becomes ready
 */

private suspend fun awaitToBeReady(
    timeout: Duration = 10.seconds
): Boolean {
    return runCatching {
        SingleInstanceManager.get().singleInstanceService().useService {
            withTimeout(timeout) {
                it.awaitReady()
            }
            true
        }
    }.getOrElse { false }
}
