package com.abdownloadmanager.android.pages.qrcode

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.ContainsScreenState
import com.abdownloadmanager.shared.util.mvi.SupportsScreenState
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext

class ScannerComponent(
    ctx: ComponentContext
) : BaseComponent(ctx),
    ContainsScreenState<ScannerComponent.State> by SupportsScreenState(State()),
    ContainsEffects<ScannerComponent.Effects> by supportEffects() {

    fun onSuccess(value: String) {
        sendEffect(
            Effects.DataReady(value)
        )
    }

    fun toggleFlashlight() {
        setState {
            it.copy(
                isFlashlightOn = !it.isFlashlightOn,
            )
        }
    }

    fun setHasFlashUnit(hasFlash: Boolean) {
        setState {
            it.copy(
                hasFlashUnit = hasFlash,
                isFlashlightOn = hasFlash && it.isFlashlightOn,
            )
        }
    }

    @Immutable
    data class State(
        val isFlashlightOn: Boolean = false,
        val hasFlashUnit: Boolean = false,
    )

    sealed interface Effects {
        data class DataReady(val data: String) : Effects
    }
}
