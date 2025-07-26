package com.abdownloadmanager.desktop.pages.poweractionalert

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.BaseComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PowerActionComponent(
    val ctx: ComponentContext,
    val powerActionConfig: PowerActionConfig,
    val powerActionReason: PowerActionReason?,
    private val powerActionDelay: Long,
    private val close: () -> Unit,
    private val onBeforePowerAction: suspend () -> Unit,
) : BaseComponent(
    ctx,
), KoinComponent {
    val applicationScope by inject<CoroutineScope>()
    val totalDelay = this@PowerActionComponent.powerActionDelay
    private val _remainingDelay = MutableStateFlow(totalDelay)
    val remainingDelay = _remainingDelay.asStateFlow()
    private val _isPerformingPowerAction = MutableStateFlow(false)
    val isShuttingDown = _isPerformingPowerAction.asStateFlow()

    private val _powerActionError = MutableStateFlow(null as Throwable?)
    val powerActionError = _powerActionError.asStateFlow()

    init {
        start()
    }

    fun start() {
        scope.launch {
            var remaining = this@PowerActionComponent.powerActionDelay
            val eachStep = 1000 / 33L
            while (remaining >= 0) {
                delay(eachStep)
                remaining = (remaining - eachStep)
                _remainingDelay.value = remaining.coerceAtLeast(0)
            }
            performPowerAction()
        }
    }

    fun performCancel() {
        close()
    }

    fun performPowerAction() {
        applicationScope.launch {
            _isPerformingPowerAction.value = true
            val success = try {
                doPowerAction()
            } catch (e: Exception) {
                _powerActionError.value = e
                e.printStackTrace()
                _isPerformingPowerAction.value = false
                false
            }
            if (success) {
                withContext(Dispatchers.Main) {
                    close()
                }
            }
        }
    }

    private suspend fun doPowerAction(): Boolean {
        onBeforePowerAction()
        delay(1000)
        return DesktopUtils.powerAction().initiate(powerActionConfig)
    }

    data class Config(
        val powerActionConfig: PowerActionConfig,
        val powerActionDelay: Long = 30_000,
        val powerActionReason: PowerActionReason? = null,
    )

    enum class PowerActionReason(
        val message: StringSource,
        val type: Type,
    ) {
        QueueWorkFinished(Res.string.system_shutdown_reason_queue_completed.asStringSource(), Type.Success),
        QueueEndTimeReached(Res.string.system_shutdown_reason_queue_end_time_reached.asStringSource(), Type.Success),
        DownloadFinished(Res.string.system_shutdown_download_finished.asStringSource(), Type.Success),
        Unknown(Res.string.unknown.asStringSource(), Type.Error);

        enum class Type {
            Success, Warning, Error,
        }
    }

}
