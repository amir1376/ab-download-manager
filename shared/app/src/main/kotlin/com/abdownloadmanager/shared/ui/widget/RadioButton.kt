package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RadioButton(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    uncheckedAlpha: Float = 0.25f,
) {
    val shape = CircleShape
    Box(
        modifier
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .size(size)
            .clip(shape)
            .triStateToggleable(
                state = ToggleableState(value),
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onValueChange(!value) },
            )
    ) {
        Spacer(
            Modifier.matchParentSize()
                .border(
                    1.dp,
                    if (value) {
                        myColors.primaryGradient
                    } else {
                        SolidColor(LocalContentColor.current / uncheckedAlpha)
                    },
                    shape
                )
        )
        AnimatedContent(
            value,
            transitionSpec = {
                val tween = tween<Float>(220)
                fadeIn(tween) togetherWith fadeOut(tween)
            }
        ) {
            val m = Modifier
                .fillMaxSize()
                .alpha(animateFloatAsState(if (value) 1f else 0f).value)
                .padding(4.dp)
                .clip(shape)
                .background(myColors.primaryGradient)
            Spacer(m)
        }
    }
}
