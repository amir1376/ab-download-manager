package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import ir.amirab.util.execAndWait

object CoreApplicationAwaker {
    fun awakeTheApp(
        timeout: Long = 10_000
    ) {
        val executable = requireNotNull(AppInfo.exeFile) {
            "We executable file detected"
        }
        execAndWait(
            arrayOf(
                executable,
                AppArguments.Args.START_IF_NOT_STARTED
            ),
            timeout,
        )
    }
}
