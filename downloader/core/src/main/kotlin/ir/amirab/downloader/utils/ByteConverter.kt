package ir.amirab.downloader.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object ByteConverter {
    const val BYTES = 1L
    const val K_BYTES = BYTES*1024L
    const val M_BYTES = K_BYTES * 1024L
    const val G_BYTES = M_BYTES * 1024L
    const val T_BYTES = G_BYTES * 1024L
    private val format = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
    fun byteTo(value: Long, unit: Long): Double {
        return (value / unit.toDouble())
    }

    fun unitToByte(value: Double, unit: Long): Long {
        return (value * unit).toLong()
    }

    fun prettify(value: Number): String {
        return format.format(value)
    }

    fun unitPrettify(unit:Long): String? {
        return when(unit){
            BYTES->"B"
            K_BYTES->"KB"
            M_BYTES->"MB"
            G_BYTES->"GB"
            T_BYTES->"TB"
            else ->null
        }
    }
}