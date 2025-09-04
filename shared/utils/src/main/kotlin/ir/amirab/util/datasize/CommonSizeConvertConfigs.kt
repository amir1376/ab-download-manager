package ir.amirab.util.datasize

object CommonSizeConvertConfigs {
    val BinaryBytes
        get() = ConvertSizeConfig(
            baseSize = BaseSize.Bytes,
            factors = SizeFactors.BinarySizeFactors,
        )
    val BinaryBits
        get() = ConvertSizeConfig(
            baseSize = BaseSize.Bits,
            factors = SizeFactors.BinarySizeFactors,
        )
    val DecimalBytes
        get() = ConvertSizeConfig(
            baseSize = BaseSize.Bytes,
            factors = SizeFactors.DecimalSizeFactors,
        )
    val DecimalBits
        get() = ConvertSizeConfig(
            baseSize = BaseSize.Bits,
            factors = SizeFactors.DecimalSizeFactors,
        )
}
