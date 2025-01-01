package ir.amirab.util.datasize

data class ConvertSizeConfig(
    val baseSize: BaseSize,
    val factors: SizeFactors,
    // default to auto
    val acceptedFactors: List<SizeFactors.FactorValue> = SizeFactors.FactorValue.entries,
)