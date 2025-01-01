package ir.amirab.util.datasize

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

data class SizeWithUnit(
    val value: Double,
    val unit: SizeUnit,
) {
    fun toString(format: NumberFormat?): String {
        val formattedValue = formatedValue(format)
        return "$formattedValue $unit"
    }

    fun formatedValue(format: NumberFormat? = DefaultFormat) = format
        ?.format(value)
        ?: value.toString()

    override fun toString(): String {
        return toString(DefaultFormat)
    }

    companion object {
        val DefaultFormat = DecimalFormat(
            "#.##", DecimalFormatSymbols(
                Locale.US,
            )
        )
    }
}