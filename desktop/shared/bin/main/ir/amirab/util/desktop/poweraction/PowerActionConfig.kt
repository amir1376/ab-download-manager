package ir.amirab.util.desktop.poweraction

data class PowerActionConfig(
    val type: Type,
    val force: Boolean,
) {
    enum class Type {
        Shutdown,
        Hibernate,
        Sleep,
    }
}

interface ContainsPowerActionConfigOnFinish {
    fun getPowerActionConfigOnFinish(): PowerActionConfig?
}
