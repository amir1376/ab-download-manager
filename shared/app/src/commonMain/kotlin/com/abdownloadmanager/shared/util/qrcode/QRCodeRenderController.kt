package com.abdownloadmanager.shared.util.qrcode

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Stable
data class QRCodeRenderController(
    val qrCodes: List<String>
) {
    val total get() = qrCodes.size
    var currentIndex by mutableIntStateOf(0)
        private set

    fun selectCurrentIndex(index: Int) {
        if (isSingle()) return
        currentIndex = index.coerceIn(qrCodes.indices)
    }

    fun selectCurrentIndex(
        update: (index: Int, total: Int) -> Int
    ) {
        selectCurrentIndex(update(currentIndex, total).coerceIn(qrCodes.indices))
    }

    val currentQRCode by derivedStateOf {
        qrCodes[currentIndex]
    }

    fun isSingle(): Boolean {
        return total == 1
    }

    fun next() {
        selectCurrentIndex { current, total ->
            val newCurrent = current + 1
            if (newCurrent < total) {
                newCurrent
            } else {
                0
            }
        }
    }

    fun previous() {
        selectCurrentIndex { current, total ->
            val newCurrent = current - 1
            if (newCurrent >= 0) {
                newCurrent
            } else {
                total - 1
            }
        }
    }
}

class QRCodePlayer(
    private val qrCodeController: QRCodeRenderController,
    private val scope: CoroutineScope,
) {
    var isPlaying by mutableStateOf(false)
    var speed by mutableStateOf(QRCodeCreator.MULTI_QR_DELAY_MILLIS.milliseconds)
    private var playJob: Job? = null
    fun close() {
        pause()
    }

    fun togglePlayback() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun pause() {
        playJob?.cancel()
        playJob = null
    }

    fun play() {
        playJob?.cancel()
        playJob = scope.launch {
            isPlaying = true
            try {
                while (isActive) {
                    val lastItem = qrCodeController.currentIndex
                    delay(speed)
                    if (lastItem != qrCodeController.currentIndex) {
                        // manually changed
                        continue
                    }
                    qrCodeController.next()
                }
            } finally {
                isPlaying = false
            }
        }
    }
}
