package com.abdownloadmanager.cli.utils

import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.SizeConverter
import ir.amirab.util.datasize.SizeWithUnit

/**
 * Shared formatting utilities for CLI output.
 *
 * Uses the project's shared datasize library (SizeConverter) for size/speed
 * formatting to avoid duplicating the framework in `shared/utils/datasize`.
 *
 * formatTimestamp uses java.util.Calendar since no shared equivalent exists.
 */
object CliFormatting {

    fun formatSize(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        return SizeConverter.bytesToSize(bytes, CommonSizeConvertConfigs.BinaryBytes).toString()
    }

    fun formatSpeed(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        val swu = SizeConverter.bytesToSize(bytes, CommonSizeConvertConfigs.BinaryBytes)
        return "${swu.formatedValue()} ${swu.unit}/s"
    }

    fun formatTimestamp(ts: Long): String {
        if (ts <= 0) return "N/A"
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = ts.coerceIn(-62135596800000L, 253402300799999L)
        }
        return "%tF %tT".format(cal, cal)
    }
}