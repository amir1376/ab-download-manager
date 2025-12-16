package ir.amirab.util.compose.modifiers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

fun Modifier.hijackClick(): Modifier {
    return silentClickable {
        // nothing
    }
}

fun Modifier.silentClickable(
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier {
    return clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
}
