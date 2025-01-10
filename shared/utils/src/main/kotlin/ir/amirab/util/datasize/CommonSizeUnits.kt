package ir.amirab.util.datasize

object CommonSizeUnits {
    val BinaryBytes = SizeUnit(
        factorValue = SizeFactors.FactorValue.None,
        baseSize = BaseSize.Bytes,
        factors = SizeFactors.BinarySizeFactors,
    )
    val BinaryBits = SizeUnit(
        factorValue = SizeFactors.FactorValue.None,
        baseSize = BaseSize.Bits,
        factors = SizeFactors.BinarySizeFactors,
    )
}