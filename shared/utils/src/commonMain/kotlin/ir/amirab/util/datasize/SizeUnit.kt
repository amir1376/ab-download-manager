package ir.amirab.util.datasize

data class SizeUnit(
    val factorValue: SizeFactors.FactorValue = SizeFactors.FactorValue.None,
    val baseSize: BaseSize,
    val factors: SizeFactors,
) {
    override fun toString(): String {
        val factor = factors.toString(factorValue)
        return "$factor$baseSize"
    }
}