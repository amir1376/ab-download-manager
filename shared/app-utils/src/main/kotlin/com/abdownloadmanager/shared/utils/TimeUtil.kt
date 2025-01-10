package com.abdownloadmanager.shared.utils

import androidx.compose.runtime.*
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSourceWithARgs
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
    return (if (isLater) {
        names.left(relativeTime)
    } else {
        names.ago(relativeTime)
    }).getString()
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
            append(names.years(years).getString())
        }
        if (used == count) return@buildString
        if (months > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(names.months(months).getString())
        }
        if (used == count) return@buildString
        if (days > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(names.days(days).getString())
        }
        if (used == count) return@buildString
        if (hours > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(names.hours(hours).getString())
        }
        if (used == count) return@buildString
        if (minutes > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(names.minutes(minutes).getString())
        }
        if (used == count) return@buildString
        if (seconds > 0) {
            if (used > 0) {
                append(" ")
            }
            used++
            append(names.seconds(seconds).getString())
        }
        if (used == count) return@buildString
        if (used == 0) {
            append(names.seconds(0).getString())
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
    fun years(years: Int): StringSource
    fun months(months: Int): StringSource
    fun days(days: Int): StringSource
    fun hours(hours: Int): StringSource
    fun minutes(minutes: Int): StringSource
    fun seconds(seconds: Int): StringSource
    fun ago(time: String): StringSource
    fun left(time: String): StringSource


    @Stable
    object SimpleNames : TimeNames {
        override fun years(years: Int): StringSource = Res.string.relative_time_long_years.asStringSourceWithARgs(
            Res.string.relative_time_long_years_createArgs(years = years.toString())
        )

        override fun months(months: Int): StringSource = Res.string.relative_time_long_months.asStringSourceWithARgs(
            Res.string.relative_time_long_months_createArgs(months = months.toString())
        )

        override fun days(days: Int): StringSource =
            Res.string.relative_time_long_days.asStringSourceWithARgs(Res.string.relative_time_long_days_createArgs(days = days.toString()))

        override fun hours(hours: Int): StringSource = Res.string.relative_time_long_hours.asStringSourceWithARgs(
            Res.string.relative_time_long_hours_createArgs(hours = hours.toString())
        )

        override fun minutes(minutes: Int): StringSource =
            Res.string.relative_time_long_minutes.asStringSourceWithARgs(
                Res.string.relative_time_long_minutes_createArgs(minutes = minutes.toString())
            )

        override fun seconds(seconds: Int): StringSource =
            Res.string.relative_time_long_seconds.asStringSourceWithARgs(
                Res.string.relative_time_long_seconds_createArgs(seconds = seconds.toString())
            )

        override fun left(time: String): StringSource =
            Res.string.relative_time_left.asStringSourceWithARgs(Res.string.relative_time_left_createArgs(time = time))

        override fun ago(time: String): StringSource =
            Res.string.relative_time_ago.asStringSourceWithARgs(Res.string.relative_time_ago_createArgs(time = time))
    }

    object ShortNames : TimeNames {
        override fun years(years: Int): StringSource = Res.string.relative_time_short_years.asStringSourceWithARgs(
            Res.string.relative_time_short_years_createArgs(years = years.toString())
        )

        override fun months(months: Int): StringSource = Res.string.relative_time_short_months.asStringSourceWithARgs(
            Res.string.relative_time_short_months_createArgs(months = months.toString())
        )

        override fun days(days: Int): StringSource = Res.string.relative_time_short_days.asStringSourceWithARgs(
            Res.string.relative_time_short_days_createArgs(days = days.toString())
        )

        override fun hours(hours: Int): StringSource = Res.string.relative_time_short_hours.asStringSourceWithARgs(
            Res.string.relative_time_short_hours_createArgs(hours = hours.toString())
        )

        override fun minutes(minutes: Int): StringSource =
            Res.string.relative_time_short_minutes.asStringSourceWithARgs(
                Res.string.relative_time_short_minutes_createArgs(minutes = minutes.toString())
            )

        override fun seconds(seconds: Int): StringSource =
            Res.string.relative_time_short_seconds.asStringSourceWithARgs(
                Res.string.relative_time_short_seconds_createArgs(seconds = seconds.toString())
            )

        override fun left(time: String): StringSource =
            Res.string.relative_time_left.asStringSourceWithARgs(Res.string.relative_time_left_createArgs(time = time))

        override fun ago(time: String): StringSource =
            Res.string.relative_time_ago.asStringSourceWithARgs(Res.string.relative_time_ago_createArgs(time = time))
    }
}

private val DefaultTimeNames = TimeNames.SimpleNames
