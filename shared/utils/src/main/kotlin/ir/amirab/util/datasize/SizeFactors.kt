package ir.amirab.util.datasize

import kotlin.math.absoluteValue
import kotlin.math.pow

sealed class SizeFactors(
    val baseValue: Long,
) {
    enum class FactorValue {
        None,
        Kilo,
        Mega,
        Giga,
        Tera,
//        Peta,
//        Exa,
    }

    operator fun get(factorValue: FactorValue): Long {
        return getFactorSize(factorValue)
    }

    private fun getFactorSize(factorValue: FactorValue): Long {
        return factors[factorValue.ordinal]
    }

    private val factors = FactorValue.entries.map {
        baseValue.toDouble().pow(it.ordinal).toLong()
    }

    fun bestFactor(
        value: Long,
        acceptedFactors: List<FactorValue> = FactorValue.entries,
    ): FactorValue {
        require(acceptedFactors.isNotEmpty()) {
            "acceptedFactors must not be empty"
        }
        // we need lowest
        if (value == 0L) {
            return acceptedFactors.first()
        }
        // no other choice
        if (acceptedFactors.size == 1) return acceptedFactors.first()
        // find in range
        val inRange = acceptedFactors.lastOrNull {
            getFactorSize(it) <= value
        }
        if (inRange != null) {
            return inRange
        }
        // find rearrest
        return acceptedFactors.minBy {
            (value - getFactorSize(it)).absoluteValue
        }
    }

    fun removeFactor(value: Double, factorValue: FactorValue): Long {
        return (value * getFactorSize(factorValue)).toLong()
    }

    fun withFactor(value: Double, factorValue: FactorValue): Double {
        if (factorValue == FactorValue.None) return value
        return value / getFactorSize(factorValue)
    }

    abstract fun toString(factorValue: FactorValue): String

    data object DecimalSizeFactors : SizeFactors(baseValue = 1000) {
        override fun toString(factorValue: FactorValue): String {
            return when (factorValue) {
                FactorValue.None -> ""
                FactorValue.Kilo -> "k"
                FactorValue.Mega -> "m"
                FactorValue.Giga -> "g"
                FactorValue.Tera -> "t"
//                FactorValue.Peta -> "p"
//                FactorValue.Exa -> "e"
            }
        }

    }

    data object BinarySizeFactors : SizeFactors(baseValue = 1024) {
        override fun toString(factorValue: FactorValue): String {
            return when (factorValue) {
                FactorValue.None -> ""
                FactorValue.Kilo -> "K"
                FactorValue.Mega -> "M"
                FactorValue.Giga -> "G"
                FactorValue.Tera -> "T"
//                FactorValue.Peta -> "P"
//                FactorValue.Exa -> "E"
            }
        }
    }
}
