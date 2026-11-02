package com.abdownloadmanager.android.pages.qrcode

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.ContainsScreenState
import com.abdownloadmanager.shared.util.mvi.SupportsScreenState
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ScannerComponent(
    ctx: ComponentContext
) : BaseComponent(ctx),
    ContainsScreenState<ScannerComponent.State> by SupportsScreenState(State()),
    ContainsEffects<ScannerComponent.Effects> by supportEffects() {
    private val qrCodeTextReader = QRCodeTextReader()
    private var lastScannedTime = 0L

    init {
        scope.launch {
            qrCodeTextReader.readChunksStats.collect { readStats ->
                setState {
                    it.copy(
                        readValues = readStats.entries.sortedBy { it.key }
                            .map { it.value }
                    )
                }
            }
        }
        scope.launch {
            val totalValue = qrCodeTextReader.resultReady.first()
            val delayForAnimation = when (state.value.total) {
                1 -> 1000L
                2 -> 800L
                3 -> 500L
                else -> 300L
            }.milliseconds
            delay(delayForAnimation)
            sendEffect(
                Effects.DataReady(totalValue)
            )
        }
    }

    fun onQRScanned(value: String) {
        lastScannedTime = System.currentTimeMillis()
        qrCodeTextReader.addSegments(value)
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
        val readValues: List<Boolean> = listOf(false),
        val isFlashlightOn: Boolean = false,
        val hasFlashUnit: Boolean = false,
    ) {
        val progress = readValues.count { it }
        val total = readValues.size
    }

    sealed interface Effects {
        data class DataReady(
            val data: String,
        ) : Effects
    }
}
