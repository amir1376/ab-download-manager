package com.abdownloadmanager.desktop.storage

import com.abdownloadmanager.shared.storage.IExtraQueueSettings
import ir.amirab.util.desktop.poweraction.ContainsPowerActionConfigOnFinish
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class DesktopExtraQueueSettings(
    override val id: Long,
    val powerActionTypeOnFinish: PowerActionConfig.Type? = null,
    val powerActionUseForceOnFinish: Boolean = false,
    /**
     * Ordered list of network interface names (or local IPs) that downloads in
     * this queue should be bound to. Concurrent downloads are spread across them
     * round-robin. An empty list means use the system default route.
     * Desktop-only (per-queue multi-homing).
     */
    val networkInterfaces: List<String> = emptyList(),
) : IExtraQueueSettings, ContainsPowerActionConfigOnFinish {

    override fun getPowerActionConfigOnFinish() = powerActionTypeOnFinish?.let {
        PowerActionConfig(
            powerActionTypeOnFinish,
            powerActionUseForceOnFinish,
        )
    }

    companion object : IExtraQueueSettings.DataClassDefinitions<DesktopExtraQueueSettings> {
        override fun createDefault(id: Long) = DesktopExtraQueueSettings(id)
        override val serializer: KSerializer<DesktopExtraQueueSettings> = DesktopExtraQueueSettings.serializer()
    }
}
