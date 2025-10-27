package com.abdownloadmanager.android.util

object AndroidConstants {
    const val SERVICE_NOTIFICATION_ID = 1
    const val NOTIFICATION_DOWNLOAD_CHANEL_ID = "downloads"
    const val NOTIFICATION_DOWNLOAD_CHANEL_NAME = "Download Manager Service"

    const val NOTIFICATION_CRASH_REPORT_CHANEL_ID = "crashReport"
    const val NOTIFICATION_CRASH_REPORT_CHANEL_NAME = "Crash Report"

    object Intents {
        private const val prefix = "com.abdownloadmanager."
        const val STOP_ALL_ACTION = prefix + "STOP_ALL"
        const val STOP_ACTION = prefix + "STOP"
        const val RESUME_ACTION = prefix + "RESUME"
        const val TOGGLE_ACTION = prefix + "TOGGLE"
        const val NOTIFICATION_DELETED = prefix + "NOTIFICATION_DELETED"

        // download id
        const val TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID = "downloadId"
        const val EXIT_ACTION = prefix + "EXIT"
    }

}
