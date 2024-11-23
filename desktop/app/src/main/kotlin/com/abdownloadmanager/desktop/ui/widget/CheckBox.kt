package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Check
import com.abdownloadmanager.utils.compose.widget.Icon

@Composable
fun CheckBox(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean=true,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    interactionSource:MutableInteractionSource= remember { MutableInteractionSource() },
    uncheckedAlpha: Float = 0.25f,
) {
    val shape = RoundedCornerShape(25)
    Box(
        modifier
            .ifThen(!enabled){
                alpha(0.5f)
            }
            .size(size)
            .clip(shape)
            .triStateToggleable(
                state = ToggleableState(value),
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onValueChange(!value) },
            )
    ) {
        Spacer(
            Modifier.matchParentSize()
                .border(1.dp, LocalContentColor.current / uncheckedAlpha, shape)
        )
        AnimatedContent(
            value,
            transitionSpec = {
                val tween= tween<Float>(220)
                fadeIn(tween) togetherWith fadeOut(tween)
            }
        ) {
            val m = Modifier
                .fillMaxSize()
                .alpha(animateFloatAsState(if (value) 1f else 0f).value)
                .background(myColors.primaryGradient)
            if (it) {
                Icon(
                    imageVector = AbIcons.Default.Check,
                    contentDescription = null,
                    modifier = m,
                    tint = myColors.onPrimaryGradient,
                )
            } else {
                Spacer(m)
            }
        }
    }
}