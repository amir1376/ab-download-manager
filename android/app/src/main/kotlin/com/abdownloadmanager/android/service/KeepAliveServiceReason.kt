package com.abdownloadmanager.android.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.MyDateAndTimeFormats
import ir.amirab.downloader.db.QueueModel
import ir.amirab.util.compose.asStringSource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface KeepAliveServiceReason {
    fun getKeyChanges(): Any
    fun getReasonString(): String
    data class ActiveDownloads(val count: Int) : KeepAliveServiceReason {
        override fun getKeyChanges() = count
        override fun getReasonString(): String {
            return Res.string.downloading.asStringSource().getString() + ": $count"
        }
    }

    data class ActiveQueue(val queueModels: List<QueueModel>) : KeepAliveServiceReason {
        override fun getKeyChanges() = queueModels.size
        override fun getReasonString(): String {
            val qNames = queueModels.joinToString(", ") { it.name }
            return "Q: $qNames ⏳"
        }
    }

    data class ScheduledQueues(val queueModels: List<QueueModel>) : KeepAliveServiceReason {
        override fun getKeyChanges() = queueModels.map { it.scheduledTimes }

        @OptIn(ExperimentalTime::class)
        override fun getReasonString(): String {
            val q = queueModels.map {
                it to it.scheduledTimes.getNearestTimeToStart()
            }.minByOrNull() { it.second } ?: return ""
            val startTime = q.second
            val instant = Instant.fromEpochMilliseconds(startTime)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val fDateTime = dateTime.format(MyDateAndTimeFormats.fullDateTimeWithoutYearAndSeconds)
            return "Q: ${q.first.name} - $fDateTime"
        }
    }

    data object AppIsInForeground : KeepAliveServiceReason {
        override fun getKeyChanges() = Unit

        override fun getReasonString(): String {
            return Res.string.idle.asStringSource().getString()
        }
    }

    @Composable
    fun rememberReasonString(): String {
        return remember(getKeyChanges()) {
            getReasonString()
        }
    }
}
