package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberComponentRectPositionProvider
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.delay

private const val TooltipDelay = 500L

@Composable
fun Tooltip(
    tooltip: StringSource,
    delayUntilShow: Long = TooltipDelay,
    anchor: Alignment = Alignment.TopCenter,
    alignment: Alignment = Alignment.TopCenter,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showHint by remember { mutableStateOf(false) }
    LaunchedEffect(isHovered) {
        showHint = isHovered
    }
    Column(
        modifier = Modifier
            .hoverable(interactionSource)
    ) {
        if (showHint) {
            DelayedTooltipPopup(
                onRequestCloseShowHelpContent = {
                    showHint = false
                },
                content = tooltip.rememberString(),
                delay = delayUntilShow,
                anchor = anchor,
                alignment = alignment
            )
        }
        content()
    }
}

@Composable
fun TooltipPopup(
    onRequestCloseShowHelpContent: () -> Unit,
    content: String,
    anchor: Alignment = Alignment.TopCenter,
    alignment: Alignment = Alignment.TopCenter
) {
    Popup(
        popupPositionProvider = rememberComponentRectPositionProvider(
            anchor = anchor,
            alignment = alignment,
        ),
        onDismissRequest = onRequestCloseShowHelpContent
    ) {
        val shape = RoundedCornerShape(6.dp)
        Box(
            Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 240.dp)
                .shadow(24.dp)
                .clip(shape)
                .border(1.dp, myColors.surface, shape)
                .background(myColors.menuGradientBackground)
                .padding(8.dp)
        ) {
            WithContentColor(myColors.onSurface) {
                Text(
                    content,
                    fontSize = myTextSizes.base,
                )
            }
        }
    }
}

@Composable
fun DelayedTooltipPopup(
    onRequestCloseShowHelpContent: () -> Unit,
    content: String,
    delay: Long = TooltipDelay,
    anchor: Alignment = Alignment.TopCenter,
    alignment: Alignment = Alignment.TopCenter,
) {
    var showPopup by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay)
        showPopup = true
    }
    if (showPopup) {
        TooltipPopup(
            onRequestCloseShowHelpContent = onRequestCloseShowHelpContent,
            content = content,
            anchor = anchor,
            alignment = alignment,
        )
    }
}
