package com.abdownloadmanager.shared.ui.widget

import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.util.div
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.modifiers.autoMirror

@Composable
fun alphaFlicker(): Float {
    val t = rememberInfiniteTransition()
    return t.animateFloat(1f, 0f, infiniteRepeatable(tween(1000), repeatMode = RepeatMode.Reverse)).value
}

@Composable
fun IconActionButton(
    icon: IconSource,
    contentDescription: StringSource,
    modifier: Modifier = Modifier,
    indicateActive: Boolean = false,
    requiresAttention: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = myShapes.defaultRounded,
    backgroundColor: Color = myColors.surface,
    contentColor: Color = LocalContentColor.current,
    borderColor: Color = myColors.onBackground / 10,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    automaticMirrorIcon: Boolean = true,
    iconSize: Dp = mySpacings.iconSize,
    onClick: () -> Unit,
) {
    Tooltip(contentDescription) {
        WithContentColor(contentColor) {
            val isFocused by interactionSource.collectIsFocusedAsState()
            val isActiveOrFocused = indicateActive || isFocused
            Box(
                modifier
                    .sizeIn(mySpacings.thumbSize, mySpacings.thumbSize)
                    .ifThen(!enabled) {
                        alpha(0.5f)
                    }
                    .border(
                        1.dp,
                        borderColor,
                        shape
                    )
                    .ifThen(isActiveOrFocused || requiresAttention) {
                        border(
                            1.dp,
                            myColors.focusedBorderColor / if (isActiveOrFocused) 1f else alphaFlicker(),
                            shape
                        )
                    }
                    .clip(shape)
                    .background(backgroundColor)
                    .clickable(
                        enabled = enabled,
                        indication = LocalIndication.current,
                        interactionSource = interactionSource,
                        role = Role.Button,
                        onClick = onClick,
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                MyIcon(
                    icon,
                    contentDescription.rememberString(),
                    Modifier
                        .ifThen(automaticMirrorIcon) {
                            autoMirror()
                        }
                        .size(iconSize),
                )
            }
        }
    }
}

@Composable
fun TransparentIconActionButton(
    icon: IconSource,
    contentDescription: StringSource,
    modifier: Modifier = Modifier,
    indicateActive: Boolean = false,
    requiresAttention: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = myShapes.defaultRounded,
    contentColor: Color = LocalContentColor.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    automaticMirrorIcon: Boolean = true,
    iconSize: Dp = mySpacings.iconSize,
    onClick: () -> Unit,
) {
    IconActionButton(
        icon = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        indicateActive = indicateActive,
        requiresAttention = requiresAttention,
        enabled = enabled,
        shape = shape,
        backgroundColor = Color.Transparent,
        contentColor = contentColor,
        borderColor = Color.Transparent,
        interactionSource = interactionSource,
        automaticMirrorIcon = automaticMirrorIcon,
        iconSize = iconSize,
        onClick = onClick,
    )
}
