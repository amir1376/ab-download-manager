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

@Composable
fun Tooltip(
    tooltip: StringSource,
    delayUntilShow: Long = 500,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showHint by remember { mutableStateOf(false) }
    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(delayUntilShow)
            showHint = true
        } else {
            showHint = false
        }
    }
    Column(
        modifier = Modifier
            .hoverable(interactionSource)
    ) {
        if (showHint) {
            TooltipPopup(
                onRequestCloseShowHelpContent = {
                    showHint = false
                },
                content = tooltip.rememberString(),
            )
        }
        content()
    }
}

@Composable
fun TooltipPopup(
    onRequestCloseShowHelpContent: () -> Unit,
    content: String,
) {
    Popup(
        popupPositionProvider = rememberComponentRectPositionProvider(
            anchor = Alignment.TopCenter,
            alignment = Alignment.TopCenter,
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