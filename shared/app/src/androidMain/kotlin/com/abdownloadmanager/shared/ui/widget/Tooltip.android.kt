package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.detectTooltip(
    state: MutableState<Boolean>,
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Main
        )

        val longPress = awaitLongPressOrCancellation(down.id)

        if (longPress != null) {
            state.value = true

            down.consume()

            // Consume everything until release
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { it.consume() }

                if (event.changes.all { !it.pressed }) {
                    break
                }
            }

            state.value = false
        }
    }
}
