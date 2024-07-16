package com.abdownloadmanager.desktop.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.DateTimePeriod
import kotlin.math.absoluteValue

fun formatTime(value: Long): String? {
    if (value < 0) {
        return null
    }
    //10_000_000
    var remaining = value
    //10_000
    val _SEC = 1000
    val _MIN = 60 * _SEC
    val _HOUR = 60 * _MIN
    val hour = remaining / _HOUR
    remaining %= _HOUR
    val min = remaining / _MIN
    remaining %= _MIN
    val sec = remaining / _SEC
    val padded: (Long) -> String = {
        "$it".padStart(2, '0')
    }
    return "${hour}:${padded(min)}:${padded(sec)}"
}

fun prettifyRelativeTime(
    duration: DateTimePeriod,
    count: Int = 1,
    names: TimeNames = DefaultTimeNames,
): String {
    val years = duration.years.absoluteValue
    val months = duration.months.absoluteValue
    val days = duration.days.absoluteValue
    val hours = duration.hours.absoluteValue
    val minutes = duration.minutes.absoluteValue
    val seconds = duration.seconds.absoluteValue

    val isLater = arrayOf(
        years,
        months,
        days,
        hours,
        minutes,
        seconds,
    ).any { it < 0 }
    return prettifyRelativeTime(
        years = years,
        months = months,
        days = days,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        isLater = isLater,
        count = count,
        names = names
    )
}

fun prettifyRelativeTime(
    years: Int = 0,
    months: Int = 0,
    days: Int = 0,
    hours: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0,
    count: Int = 1,
    names: TimeNames = DefaultTimeNames,
    isLater: Boolean,
): String {
    val relativeTime = relativeTime(
        years = years,
        months = months,
        days = days,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        count = count,
        names = names,
    )
    val leftOrAgo =
            if (isLater) names.left
            else names.ago
    return "$relativeTime $leftOrAgo"
}

private fun relativeTime(
    years: Int,
    months: Int,
    days: Int,
    hours: Int,
    minutes: Int,
    seconds: Int,
    count: Int = 6,
    names: TimeNames = DefaultTimeNames,
): String {
    require(count > 0)
    var used = 0
    val relativeTime = buildString {
        if (years > 0) {
            used++
            append(years)
            append(" ${names.years}")
        }
        if (used == count) return@buildString
        if (months > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(months)
            append(" ${names.months}")
        }
        if (used == count) return@buildString
        if (days > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(days)
            append(" ${names.days}")
        }
        if (used == count) return@buildString
        if (hours > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(hours)
            append(" ${names.hours}")
        }
        if (used == count) return@buildString
        if (minutes > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(minutes)
            append(" ${names.minutes}")
        }
        if (used == count) return@buildString
        if (seconds > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(seconds)
            append(" ${names.seconds}")
        }
        if (used == count) return@buildString
        if (used == 0) {
            append("0 ${names.seconds}")
        }
    }
    return relativeTime
}


@Composable
fun rememberTimeFormatedValue(value: Long): String? {
    return remember(value) {
        formatTime(value)
    }
}

@Composable
fun rememberTimeEllapsed(value: Long): Long {
    var result by remember { mutableStateOf(0L) }
    LaunchedEffect(value) {
        while (isActive) {
            result = if (value < 0) {
                -1
            } else {
                System.currentTimeMillis() - value
            }
            delay(1_000)
        }
    }
    return result
}

fun convertTimeRemainingToHumanReadable(
    totalSecs: Long,
    timeNames: TimeNames = TimeNames.SimpleNames,
): String {
    val hours = totalSecs / 3600;
    val minutes = (totalSecs % 3600) / 60;
    val seconds = totalSecs % 60;
    return prettifyRelativeTime(
        hours = hours.toInt(),
        minutes = minutes.toInt(),
        seconds = seconds.toInt(),
        isLater = true,
        count = 3,
        names = timeNames,
    )
}

@Stable
interface TimeNames {
    val years: String
    val months: String
    val days: String
    val hours: String
    val minutes: String
    val seconds: String
    val ago: String
    val left: String


    @Stable
    object SimpleNames : TimeNames {
        override val years: String = "years"
        override val months: String = "months"
        override val days: String = "days"
        override val hours: String = "hours"
        override val minutes: String = "minutes"
        override val seconds: String = "seconds"
        override val left: String = "left"
        override val ago: String = "ago"
    }

    object ShortNames : TimeNames {
        override val years: String = "yr"
        override val months: String = "mn"
        override val days: String = "d"
        override val hours: String = "hr"
        override val minutes: String = "m"
        override val seconds: String = "s"
        override val left: String = "left"
        override val ago: String = "ago"
    }
}

private val DefaultTimeNames = TimeNames.SimpleNames
