package ir.amirab.util.desktop.poweraction

interface PowerAction {
    fun initiate(config: PowerActionConfig): Boolean
}
