package com.abdownloadmanager.desktop.ui.util

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import jdk.jfr.Enabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
class HoveredState {
    val isHovered = mutableStateOf(false)
}

fun Modifier.listenToHoveredState(
    hoveredState: HoveredState,
    enabled: Boolean=true
): Modifier {
    return composed {
        val mis= remember{
            MutableInteractionSource()
        }
        LaunchedEffect(Unit){
            mis.interactions.filterIsInstance<HoverInteraction>()
                .onEach {
                    when(it){
                        is HoverInteraction.Enter->{
                            hoveredState.isHovered.value=true
                        }
                        is HoverInteraction.Exit->{
                            hoveredState.isHovered.value=false
                        }
                        else->{

                        }
                    }
                }.launchIn(this)
        }

        hoverable(mis,enabled)
    }
}

