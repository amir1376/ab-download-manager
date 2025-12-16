package com.abdownloadmanager.android.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.ifThen

object SelectionControlsScope

@Composable
fun RenderControlSelections(
    onRequestSelectAll: () -> Unit,
    onRequestSelectInside: () -> Unit,
    onRequestInvertSelection: () -> Unit,
    selectionCount: Int,
    total: Int,
    otherActions: @Composable SelectionControlsScope.() -> Unit
) {
    with(SelectionControlsScope) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RenderSelectAll(
                onClick = onRequestSelectAll,
                Modifier,
            )
            RenderSelectInside(
                onClick = onRequestSelectInside,
                Modifier,
            )
            RenderInvertSelection(
                onClick = onRequestInvertSelection,
                Modifier,
            )
            Text(
                "$selectionCount / $total",
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            otherActions()
        }
    }
}

@Composable
context(_: SelectionControlsScope)
private fun RenderSelectAll(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    SelectionControlButton(
        icon = MyIcons.selectAll,
        contentDescription = Res.string.select_all.asStringSource(),
        modifier = modifier,
        enabled = true,
        toggledOff = false,
        onClick = {
            onClick()
        },
        padding = PaddingValues(12.dp),
    )
}

@Composable
context(_: SelectionControlsScope)
private fun RenderSelectInside(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    SelectionControlButton(
        icon = MyIcons.selectInside,
        contentDescription = Res.string.select_inside.asStringSource(),
        modifier = modifier,
        enabled = true,
        toggledOff = false,
        onClick = {
            onClick()
        },
        padding = PaddingValues(12.dp),
    )
}

@Composable
context(_: SelectionControlsScope)
private fun RenderInvertSelection(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    SelectionControlButton(
        icon = MyIcons.selectInvert,
        contentDescription = Res.string.select_invert.asStringSource(),
        modifier = modifier,
        enabled = true,
        toggledOff = false,
        onClick = {
            onClick()
        },
    )
}

@Composable
context(_: SelectionControlsScope)
fun SelectionControlButton(
    icon: IconSource,
    contentDescription: StringSource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    padding: PaddingValues = PaddingValues(12.dp),
    toggledOff: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
) {
    val size: Dp = 24.dp
    val isFocused by interactionSource.collectIsFocusedAsState()
    Box(
        modifier
            .ifThen(!enabled || toggledOff) {
                alpha(0.5f)
            }
            .ifThen(isFocused) {
                border(
                    1.dp,
                    myColors.focusedBorderColor,
                    shape
                )
            }
            .clip(shape)
            .clickable(
                enabled = enabled,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(padding)
    ) {
        MyIcon(
            icon,
            contentDescription.rememberString(),
            Modifier
                .size(size)
        )
    }
}
