package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.utils.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.width(42.dp).height(24.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(
        modifier
            .clip(CircleShape)
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .background(
                if (checked) {
                    myColors.primaryGradient
                }else {
                    Brush.linearGradient(listOf(Color.Gray,Color.Gray))
                }
            )
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(4.dp)
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .aspectRatio(1f, true)
                .align(
                    BiasAlignment(
                        animateFloatAsState(
                            if (checked) 1f else -1f
                        ).value,
                        0f,
                    )
                )
                .clip(CircleShape)
                .background(myColors.onPrimaryGradient / animateFloatAsState(if (checked) 1f else 0.5f).value)
        )
    }
}