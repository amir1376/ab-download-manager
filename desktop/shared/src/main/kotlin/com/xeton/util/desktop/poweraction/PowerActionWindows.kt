package com.xeton.util.desktop.poweraction

import com.xeton.util.execAndWait

class PowerActionWindows : PowerAction {
    override fun initiate(config: PowerActionConfig): Boolean {
        return when (config.type) {
            PowerActionConfig.Type.Shutdown -> shutdown(config.force)
            PowerActionConfig.Type.Hibernate -> TODO()
            PowerActionConfig.Type.Sleep -> TODO()
        }
    }

    private fun shutdown(force: Boolean): Boolean {
        val command = arrayOf("shutdown", "/s", "/t", "0")
        return execAndWait(command)
    }
}
