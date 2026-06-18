package ir.amirab.util.desktop.poweraction

import ir.amirab.util.execAndWait

class PowerActionLinux : PowerAction {
    override fun initiate(config: PowerActionConfig): Boolean {
        return when (config.type) {
            PowerActionConfig.Type.Shutdown -> shutdown(config.force)
            PowerActionConfig.Type.Hibernate -> TODO()
            PowerActionConfig.Type.Sleep -> TODO()
        }
    }

    private fun shutdown(force: Boolean): Boolean {
        val commands = listOf(
            arrayOf(
                "dbus-send", "--system", "--print-reply",
                "--dest=org.freedesktop.login1",
                "/org/freedesktop/login1",
                "org.freedesktop.login1.Manager.PowerOff",
                "boolean:true",
            ),
            arrayOf(
                "systemctl", "poweroff"
            ),
        )
        return commands.any { command ->
            runCatching {
                execAndWait(command)
            }.getOrElse { false }
        }
    }
}
