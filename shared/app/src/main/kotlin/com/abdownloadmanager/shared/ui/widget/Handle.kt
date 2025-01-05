package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skiko.Cursor

@Composable
fun Handle(
    modifier: Modifier,
    orientation: Orientation = Orientation.Horizontal,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    color: Color = myColors.surface,
    inactiveColor: Color = myColors.surface / 50,
    onDrag: (Dp) -> Unit,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isDragging by interactionSource.collectIsDraggedAsState()

    val hoverIcon = remember(orientation) {
        PointerIcon(
            Cursor(
                when (orientation) {
                    Orientation.Vertical -> Cursor.S_RESIZE_CURSOR
                    Orientation.Horizontal -> Cursor.E_RESIZE_CURSOR
                }
            )
        )
    }

    Spacer(
        modifier
            .pointerHoverIcon(hoverIcon, true)
            .hoverable(interactionSource)
            .resizeHandle(
                orientation = orientation,
                interactionSource = interactionSource,
                onDrag = onDrag,
            )
            .background(
                animateColorAsState(
                    if (isHovered || isDragging) color
                    else inactiveColor
                ).value
            )
    )
}

fun Modifier.resizeHandle(
    orientation: Orientation = Orientation.Horizontal,
    interactionSource: MutableInteractionSource? = null,
    onDrag: (Dp) -> Unit,
) = composed {
    val latestOnDrag by rememberUpdatedState(onDrag)
    val density = LocalDensity.current
    val draggableState = rememberDraggableState {
        density.run {
            latestOnDrag(it.toDp())
        }
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val reverseDirection = orientation == Orientation.Horizontal && isRtl
    draggable(
        state = draggableState,
        orientation = orientation,
        interactionSource = interactionSource,
        reverseDirection = reverseDirection
    )
}