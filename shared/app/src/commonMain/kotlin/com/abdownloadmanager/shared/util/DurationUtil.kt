package com.abdownloadmanager.shared.util

import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlin.math.roundToInt

/**
 * @param duration duration in seconds
 */
fun convertDurationToHumanReadable(duration: Double): StringSource {
    // omit fractional section
    val duration = duration.roundToInt()
    val seconds = duration % 60
    val minutes = (duration / 60) % 60
    val hours = duration / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }.asStringSource()
}
