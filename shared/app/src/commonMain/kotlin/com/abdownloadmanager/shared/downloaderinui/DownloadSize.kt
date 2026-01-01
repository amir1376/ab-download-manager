package com.abdownloadmanager.shared.downloaderinui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.abdownloadmanager.shared.util.LocalSizeUnit
import com.abdownloadmanager.shared.util.convertDurationToHumanReadable
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import ir.amirab.util.compose.StringSource
import ir.amirab.util.datasize.ConvertSizeConfig

sealed class DownloadSize : Comparable<DownloadSize> {
    abstract fun comparePriority(): Int
    abstract fun plus(other: DownloadSize): DownloadSize

    data class Bytes(
        val bytes: Long,
    ) : DownloadSize() {
        fun asStringSource(
            sizeUnit: ConvertSizeConfig
        ): StringSource {
            return convertPositiveSizeToHumanReadable(bytes, sizeUnit)
        }

        override fun comparePriority() = 2
        override fun plus(other: DownloadSize): DownloadSize {
            return if (other !is Bytes || other.bytes == 0L) {
                this
            } else {
                return Bytes(bytes + other.bytes)
            }
        }

        override fun compareTo(other: DownloadSize): Int {
            if (other is Bytes) {
                return bytes.compareTo(other.bytes)
            }
            return super.compareTo(other)
        }
        companion object{
            val Zero = Bytes(0)
        }
    }

    data class Duration(val duration: Double) : DownloadSize() {
        fun asStringSource(): StringSource {
            return convertDurationToHumanReadable(duration)
        }

        override fun comparePriority() = 1
        override fun plus(other: DownloadSize): DownloadSize {
            return if (other !is Duration || other.duration == 0.0) {
                this
            } else {
                Duration(duration + other.duration)
            }
        }

        override fun compareTo(other: DownloadSize): Int {
            if (other is Duration) {
                return duration.compareTo(other.duration)
            }
            return super.compareTo(other)
        }
        companion object{
            val Zero = Duration(0.0)
        }
    }

    override fun compareTo(other: DownloadSize): Int {
        return comparePriority().compareTo(other.comparePriority())
    }
}

@Composable
fun DownloadSize.rememberString(): String {
    return when (this) {
        is DownloadSize.Bytes -> {
            val sizeUnit = LocalSizeUnit.current
            remember(this, sizeUnit) {
                asStringSource(sizeUnit)
            }
        }

        is DownloadSize.Duration -> {
            remember(this) {
                asStringSource()
            }
        }
    }.rememberString()
}
