package com.abdownloadmanager.desktop.pages.addDownload.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.util.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.desktop.window.moveSafe
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale
import java.awt.MouseInfo

@Composable
fun <T> DialogDropDown(
    selectedItem: T?,
    possibleItems: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    dropdownOpen: Boolean,
    onRequestOpenDropDown: () -> Unit,
    onRequestCloseDropDown: () -> Unit,
    dropDownSize: DpSize = DpSize(220.dp, 250.dp),
    renderItem: @Composable (T) -> Unit,
    renderEmpty: @Composable () -> Unit,
) {
    Column(modifier) {
        DropDownHeader(
            item = selectedItem,
            enabled = enabled,
            onClick = onRequestOpenDropDown,
            renderItem = renderItem
        )
        if (dropdownOpen) {
            DropDownContent(
                closeDialog = onRequestCloseDropDown,
                dropDownSize = dropDownSize,
                possibleItems = possibleItems,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                drawOnEmpty = renderEmpty,
                renderItem = renderItem,
            )
        }
    }
}

@Composable
fun <T> DropDownContent(
    closeDialog: () -> Unit,
    dropDownSize: DpSize,
    possibleItems: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    drawOnEmpty: @Composable () -> Unit,
    renderItem: @Composable (T) -> Unit,
) {
    BaseOptionDialog(
        onCloseRequest = closeDialog,
        state = rememberDialogState(
            size = dropDownSize.applyUiScale(LocalUiScale.current)
        ),
        resizeable = true,
        content = {
            LaunchedEffect(window) {
                window.moveSafe(
                    MouseInfo.getPointerInfo().location.run {
                        DpOffset(
                            x = x.dp,
                            y = y.dp
                        )
                    }
                )
            }
            val shape = myShapes.defaultRounded

            Box(
                Modifier
                    .clip(shape)
                    .border(2.dp, myColors.onBackground / 10, shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                myColors.surface,
                                myColors.background,
                            )
                        )
                    )
            ) {
                val listState = rememberLazyListState()
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    state = listState,
                ) {
                    items(possibleItems) {
                        val isSelected = it == selectedItem
                        WithContentAlpha(
                            if (isSelected) 1f else 0.75f
                        ) {
                            Row(
                                Modifier
                                    .clip(shape)
                                    .clickable {
                                        onItemSelected(it)
                                        closeDialog()
                                    }
                                    .padding(
                                        vertical = 8.dp,
                                        horizontal = 8.dp
                                    )
                            ) {
                                Box(
                                    Modifier.weight(1f)
                                ) {
                                    renderItem(it)
                                }
                                val selectedIconModifier = Modifier.size(16.dp)
                                if (isSelected) {
                                    MyIcon(
                                        MyIcons.check,
                                        null,
                                        selectedIconModifier,
                                    )
                                } else {
                                    Spacer(selectedIconModifier)
                                }
                            }
                        }
                    }
                }
                if (possibleItems.isEmpty()) {
                    Box(Modifier.padding().fillMaxSize()) {
                        drawOnEmpty()
                    }
                }
                AnimatedVisibility(
                    visible = listState.canScrollForward,
                    modifier = Modifier.matchParentSize(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Spacer(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Transparent,
                                        0.8f to Color.Transparent,
                                        1f to myColors.background,
                                    )
                                )
                            )
                    )
                }
            }
        }
    )
}

@Composable
private fun <T> DropDownHeader(
    item: T?,
    enabled: Boolean,
    onClick: () -> Unit,
    renderItem: @Composable (T) -> Unit,
) {
    val borderColor = myColors.onBackground / 0.1f
    val background = myColors.surface / 50
    val shape = myShapes.defaultRounded
    Row(
        Modifier
            .height(IntrinsicSize.Max)
            .clip(shape)
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .border(1.dp, borderColor, shape)
            .background(background)
            .clickable(
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        val contentModifier = Modifier
            .padding(vertical = 8.dp)
            .weight(1f)
        if (item != null) {
            Box(contentModifier) {
                renderItem(item)
            }
        } else {
            Text(
                myStringResource(Res.string.no_category_selected),
                contentModifier
            )
        }
        Spacer(
            Modifier
                .padding(horizontal = 8.dp)
                .fillMaxHeight().padding(vertical = 1.dp)
                .width(1.dp)
                .background(borderColor)
        )
        MyIcon(
            MyIcons.down,
            null,
            Modifier
                .align(Alignment.CenterVertically)
                .size(16.dp),
        )
    }
}
