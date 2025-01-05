package com.abdownloadmanager.shared.ui.widget

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.menu.MyDropDown
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource

@Composable
fun IconPick(
    selectedIcon: IconSource?,
    icons: List<IconSource>,
    onSelected: (IconSource) -> Unit,
    onCancel: () -> Unit,
) {
    MyDropDown(
        onDismissRequest = onCancel,
        offset = DpOffset(y = 2.dp, x = 0.dp),
        content = {
            val shape = RoundedCornerShape(6.dp)
            Box(
                Modifier
                    .shadow(24.dp)
//                .verticalScroll(rememberScrollState())
                    .clip(shape)
//                    .width(IntrinsicSize.Max)
                    .widthIn(120.dp)
                    .height(220.dp)
                    .border(1.dp, myColors.surface, shape)
                    .background(myColors.menuGradientBackground)

            ) {
                Content(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                    selectedIcon = selectedIcon,
                    icons = icons,
                    onSelected = onSelected,
                )
            }
        }
    )
}

@Composable
private fun Content(
    modifier: Modifier,
    selectedIcon: IconSource?,
    icons: List<IconSource>,
    onSelected: (IconSource) -> Unit,
) {
    val state = rememberLazyListState()
    Box {
        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = PaddingValues(vertical = 8.dp),
            content = {
                val shape = RoundedCornerShape(6.dp)
                items(icons.chunked(6)) { rowItems ->
                    Row {
                        for (iconSource in rowItems) {
                            val isSelected = selectedIcon == iconSource
                            MyIcon(
                                iconSource,
                                null,
                                Modifier
                                    .clip(shape)
                                    .ifThen(isSelected) {
                                        background(myColors.primary / 0.25f)
                                    }
                                    .border(
                                        1.dp,
                                        if (isSelected) myColors.primary / 0.25f
                                        else Color.Transparent,
                                        shape
                                    )
                                    .clickable {
                                        onSelected(iconSource)
                                    }
                                    .padding(8.dp)
                                    .size(24.dp),
                            )
                        }
                    }
                }
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(6),
//        content = {
//            val shape = RoundedCornerShape(6.dp)
//            items(icons) {
//                MyIcon(
//                    it,
//                    null,
//                    Modifier
//                        .clip(shape)
//                        .ifThen(selectedIcon == it) {
//                            background(myColors.primary / 0.25f)
//                        }
//                        .clickable {
//                            onSelected(it)
//                        }
//                        .padding(8.dp)
//                        .size(24.dp),
//                )
//            }
//        }
//    )
            }
        )
        AnimatedVisibility(
            state.canScrollForward,
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
