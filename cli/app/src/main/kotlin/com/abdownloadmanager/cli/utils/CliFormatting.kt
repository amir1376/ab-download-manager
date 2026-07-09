package com.abdownloadmanager.cli.utils

/**
 * Shared formatting utilities for CLI output.
 *
 * formatTimestamp uses java.util.Calendar since no shared equivalent exists.
 * Size/speed formatting is delegated to SizeConverter (see :shared:utils)
 * per maintainer directive — callers inline SizeConverter.bytesToSize directly.
 */
object CliFormatting {

    fun formatTimestamp(ts: Long): String {
        if (ts <= 0) return "N/A"
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = ts.coerceIn(-62135596800000L, 253402300799999L)
        }
        return "%tF %tT".format(cal, cal)
    }
}