package ir.amirab.util.datasize

fun SizeUnit.asConverterConfig(
    acceptedFactors: List<SizeFactors.FactorValue> = listOf(
        factorValue
    ),
): ConvertSizeConfig {
    return ConvertSizeConfig(
        factors = factors,
        baseSize = baseSize,
        acceptedFactors = acceptedFactors
    )
}

fun ConvertSizeConfig.bits() = copy(
    baseSize = BaseSize.Bits
)

fun ConvertSizeConfig.bytes() = copy(
    baseSize = BaseSize.Bytes
)

fun ConvertSizeConfig.decimal() = copy(
    factors = SizeFactors.DecimalSizeFactors
)

fun ConvertSizeConfig.binary() = copy(
    factors = SizeFactors.BinarySizeFactors
)

fun ConvertSizeConfig.autoSelectFactors() = copy(
    acceptedFactors = SizeFactors.FactorValue.entries
)

fun ConvertSizeConfig.fixedFactor(factorValue: SizeFactors.FactorValue) = copy(
    acceptedFactors = listOf(factorValue)
)