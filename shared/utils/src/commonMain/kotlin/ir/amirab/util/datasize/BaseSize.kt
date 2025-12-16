package ir.amirab.util.datasize

sealed class BaseSize(
    val size: Long,
) {
    abstract fun longString(): String
    fun scaleInto(baseSize: BaseSize): Double {
        return when {
            baseSize == this -> 1.0
            else -> size / baseSize.size.toDouble()
        }
    }

    data object Bits : BaseSize(1) {
        override fun toString(): String {
            return "b"
        }

        override fun longString(): String {
            return "Bits"
        }
    }

    data object Bytes : BaseSize(8) {
        override fun toString(): String {
            return "B"
        }

        override fun longString(): String {
            return "Bytes"
        }
    }


}