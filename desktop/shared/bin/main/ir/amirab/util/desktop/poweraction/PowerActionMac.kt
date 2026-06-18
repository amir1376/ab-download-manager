package ir.amirab.util.desktop.poweraction

import ir.amirab.util.execAndWait

class PowerActionMac : PowerAction {
    override fun initiate(config: PowerActionConfig): Boolean {
        return when (config.type) {
            PowerActionConfig.Type.Shutdown -> shutdown(config.force)
            PowerActionConfig.Type.Hibernate -> TODO()
            PowerActionConfig.Type.Sleep -> TODO()
        }
    }

    private fun shutdown(force: Boolean): Boolean {
        return execAndWait(
            arrayOf(
                "osascript", "-e", "tell application \"System Events\" to shut down"
            )
        )
    }
}
