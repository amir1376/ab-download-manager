package com.abdownloadmanager.android.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.myCombinedClickable(
    onClick: ((offset: Offset) -> Unit)? = null,
    onLongClick: ((offset: Offset) -> Unit)? = null,
    onDoubleClick: ((offset: Offset) -> Unit)? = null,
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
): Modifier {
    return pointerInput(
        interactionSource,
        onClick,
        onLongClick,
        onDoubleClick,
    ) {
        detectTapGestures(
            onPress = { offset ->
                interactionSource?.let { mutableInteractionSource ->
                    val press = PressInteraction.Press(offset)
                    mutableInteractionSource.emit(press)
                    awaitRelease()
                    mutableInteractionSource.emit(PressInteraction.Release(press))
                }
            },
            onTap = onClick,
            onLongPress = onLongClick,
            onDoubleTap = onDoubleClick
        )
    }.let {
        if (interactionSource != null && indication != null) {
            it.indication(interactionSource, indication)
        } else it
    }
}
