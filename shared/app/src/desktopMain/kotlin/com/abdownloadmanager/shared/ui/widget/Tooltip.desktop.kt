package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope


actual fun Modifier.detectTooltip(
    state: MutableState<Boolean>,
): Modifier {
    return pointerInput(state) {
        coroutineScope {
            awaitPointerEventScope {
                val pass = PointerEventPass.Main

                while (true) {
                    val event = awaitPointerEvent(pass)
                    val inputType = event.changes[0].type
                    if (inputType == PointerType.Mouse) {
                        when (event.type) {
                            PointerEventType.Enter -> {
                                state.value = true
                            }

                            PointerEventType.Exit -> {
                                state.value = false
                            }
                        }
                    }
                }
            }
        }
    }
}
