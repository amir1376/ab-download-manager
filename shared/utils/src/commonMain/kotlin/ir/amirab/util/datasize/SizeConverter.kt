package ir.amirab.util.datasize

object SizeConverter {
    fun sizeToBytes(
        sizeWithUnit: SizeWithUnit,
    ): Long {
        return convert(
            sizeWithUnit,
            CommonSizeConvertConfigs
                .BinaryBytes
                .fixedFactor(SizeFactors.FactorValue.None)
        ).value.toLong()
    }

    fun bytesToSize(
        bytes: Long,
        target: ConvertSizeConfig,
    ): SizeWithUnit {
        return convert(
            SizeWithUnit(
                bytes.toDouble(),
                CommonSizeUnits.BinaryBytes,
            ),
            target
        )
    }

    fun convert(
        src: SizeWithUnit,
        target: ConvertSizeConfig,
    ): SizeWithUnit {
        val valueWithoutFactor = src.unit.factors.removeFactor(
            src.value, src.unit.factorValue
        )
        val valueWithBaseSize = valueWithoutFactor * src.unit.baseSize.scaleInto(target.baseSize)
        val factorValue = target.factors.bestFactor(
            valueWithBaseSize.toLong(),
            target.acceptedFactors,
        )
        val finalValue = target.factors.withFactor(valueWithBaseSize, factorValue)
        return SizeWithUnit(
            value = finalValue,
            SizeUnit(
                factorValue = factorValue,
                factors = target.factors,
                baseSize = target.baseSize,
            )
        )
    }
}