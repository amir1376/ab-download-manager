package ir.amirab.downloader.queue

import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleTimes(
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val enabledStartTime: Boolean,
    val enabledEndTime: Boolean,
) {
    init {
        require(daysOfWeek.isNotEmpty()) {
            "we have always have one day"
        }
    }

    companion object {
        fun default() = ScheduleTimes(
            daysOfWeek = DayOfWeek.entries.toSet(),
            startTime = LocalTime(2, 30),
            endTime = LocalTime(7, 30),
            enabledStartTime = false,
            enabledEndTime = false,
        )
    }

    private fun containsThisDay(day: DayOfWeek): Boolean {
        return day in daysOfWeek
    }


    private fun getNearestDayOfWork(forTime: LocalTime): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var currentDay = now.dayOfWeek
        val currentTime = now.time
        var count = 0

        while (true) {
            if (containsThisDay(currentDay)) {
                if (count == 0) {
                    if (currentTime < forTime) {
                        return count // ==0 today
                    }
                    //else => today's start time has been passed so we don't want today
                    //we continue for tomorrow
                } else {
                    return count
                }
            }
            currentDay = currentDay.plus(1)
            count++
            if (count > 7) {
                error("there is a bug in our code stoping loop")
            }
        }

    }

    fun getNearestTimeToStart(): Long {
        val now = Clock.System.now()

        val nextTime = now
            .plus(
                getNearestDayOfWork(startTime),
                DateTimeUnit.DAY,
                TimeZone.currentSystemDefault()
            )
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .let {
                LocalDateTime(it.date, startTime)
            }.toInstant(TimeZone.currentSystemDefault())
        return (nextTime - now).inWholeMilliseconds
    }

    fun getNearestTimeToStop(): Long {
        val stopTime = this.endTime
        val now = Clock.System.now()
        val nextTime = now
            .plus(
                getNearestDayOfWork(stopTime),
                DateTimeUnit.DAY,
                TimeZone.currentSystemDefault()
            )
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .let {
                LocalDateTime(it.date, stopTime)
            }.toInstant(TimeZone.currentSystemDefault())
        return (nextTime - now).inWholeMilliseconds
    }
}
