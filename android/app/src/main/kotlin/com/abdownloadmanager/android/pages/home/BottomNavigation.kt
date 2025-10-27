package com.abdownloadmanager.android.pages.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.home.sections.sort.DownloadSortBy
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.sort.Sort
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.db.QueueModel
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

object BottomNavigationConstants {
    const val DEFAULT_ICON_SIZE = 20
    const val DEFAULT_ICON_PADDING = 16
}

@Composable
fun BottomNavigation(
    modifier: Modifier,
    component: HomeComponent,
) {
    val isShowingSearch by component.isShowingSearch.collectAsState()
    val shouldShowMainButton = !isShowingSearch
    val isShowingAddMenu by component.isAddMenuShowing.collectAsState()
    Row(
        modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val shape = myShapes.defaultRounded
        Box(
            Modifier
                .weight(1f)
                .height(IntrinsicSize.Max)
                .shadow(4.dp, shape)
                .clip(shape)
                .border(1.dp, myColors.onSurface / 0.1f, shape)
                .background(myColors.surface)
        ) {
            AnimatedContent(
                isShowingSearch,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (it) {
                        SearchBox(
                            text = component.filterState.textToSearch,
                            onValueChange = {
                                component.filterState.textToSearch = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            onDismissRequest = {
                                component.setIsShowingSearch(false)
                            }
                        )
                    } else {
                        DefaultItems(component)
                    }
                }
            }
        }
        AnimatedVisibility(
            shouldShowMainButton
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Column {
                    RenderAddMenu(component)
                    MainBottonNavigationItem(
                        icon = MyIcons.add,
                        contentDescription = Res.string.add.asStringSource(),
                        onClick = {
                            component.setIsAddMenuShowing(!isShowingAddMenu)
                        },
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBox(
    text: String,
    onValueChange: (search: String) -> Unit,
    modifier: Modifier,
    onDismissRequest: () -> Unit,
) {
    BackHandler {
        onDismissRequest()
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    MyTextField(
        text = text,
        onTextChange = onValueChange,
        placeholder = myStringResource(Res.string.search),
        modifier = modifier
            .focusRequester(focusRequester),
        start = {
            MyIcon(
                MyIcons.search, null,
                Modifier
                    .align(Alignment.CenterVertically)
                    .padding(BottomNavigationConstants.DEFAULT_ICON_PADDING.dp)
                    .size(BottomNavigationConstants.DEFAULT_ICON_SIZE.dp),
                tint = LocalContentColor.current / 0.5f
            )
        },
        end = {
            MyIcon(
                MyIcons.clear, null,
                Modifier
                    .fillMaxHeight()
                    .clickable {
                        if (text.isEmpty()) {
                            onDismissRequest()
                        } else {
                            onValueChange("")
                        }
                    }
                    .padding(BottomNavigationConstants.DEFAULT_ICON_PADDING.dp)
                    .size(BottomNavigationConstants.DEFAULT_ICON_SIZE.dp)
                    .wrapContentHeight()
                    .alpha(0.5f),
                tint = LocalContentColor.current / 0.5f
            )
        }
    )
}

@Composable
fun RowScope.DefaultItems(
    component: HomeComponent,
) {
    val isMainMenuShowing by component.isMainMenuShowing.collectAsState()
    val isCategoryFilterMenuShowing by component.isCategoryFilterShowing.collectAsState()
    val isSortMenuShowing by component.isSortMenuShowing.collectAsState()

    val modifier = Modifier
    Column {
        RenderMainMenu(component)
        BottonNavigationItem(
            icon = MyIcons.menu,
            contentDescription = Res.string.menu.asStringSource(),
            onClick = {
                component.setIsMainMenuShowing(!isMainMenuShowing)
            },
            modifier = modifier,
            isSelected = isMainMenuShowing,
        )
    }
    BottonNavigationItem(
        icon = MyIcons.search,
        contentDescription = Res.string.search.asStringSource(),
        onClick = {
            component.setIsShowingSearch(true)
        },
        modifier = modifier,
        isSelected = false, // search bar replaced with total bottomNavigation
    )
    Spacer(
        Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(myColors.onSurface / 0.1f)
    )
    val filterMode = component.filterMode.value
    when (filterMode) {
        is HomeComponent.FilterMode.Queue -> {
            QueueIndicator(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                filterMode,
                isSelected = isCategoryFilterMenuShowing
            ) {
                component.setIsCategoryFilterShowing(!isCategoryFilterMenuShowing)
            }
        }

        is HomeComponent.FilterMode.Status -> {
            FilterStatusIndicator(
                component,
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                filterMode,
                isSelected = isCategoryFilterMenuShowing,
                onClick = {
                    component.setIsCategoryFilterShowing(!isCategoryFilterMenuShowing)
                }
            )
        }
    }
    Spacer(
        Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(myColors.onSurface / 0.1f)
    )
    when (filterMode) {
        is HomeComponent.FilterMode.Status -> {
            SortIndicator(
                component.selectedSort.collectAsState().value,
                {
                    component.setIsSortMenuShowing(!isSortMenuShowing)
                },
                modifier = Modifier,
                isSelected = isSortMenuShowing,
            )
        }

        is HomeComponent.FilterMode.Queue -> {
            ToggleQueueStatus(component, filterMode.queue)
        }
    }
}

@Composable
fun ToggleQueueStatus(
    homeComponent: HomeComponent,
    queueModel: QueueModel
) {
    val queue = remember(queueModel.id) {
        homeComponent.queueManager.getQueue(queueModel.id)
    }
    val isQueueActive by queue.activeFlow.collectAsState()
    val icon: IconSource
    val contentDescription: StringSource
    val onClick: () -> Unit
    if (isQueueActive) {
        icon = MyIcons.queueStop
        contentDescription = Res.string.stop_queue.asStringSource()
        onClick = { homeComponent.startQueue(queue.id) }
    } else {
        icon = MyIcons.queueStart
        contentDescription = Res.string.start_queue.asStringSource()
        onClick = { homeComponent.stopQueue(queue.id) }
    }
    BottonNavigationItem(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = Modifier,
        isSelected = isQueueActive,
    )
}

@Composable
private fun MainBottonNavigationItem(
    icon: IconSource,
    contentDescription: StringSource,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier) {
        val shape = myShapes.defaultRounded
        MyIcon(
            icon = icon,
            contentDescription = contentDescription.rememberString(),
            modifier = Modifier
                .shadow(4.dp, shape)
                .border(
                    1.dp,
                    myColors.primaryGradient,
                    shape,
                )
                .clip(shape)
                .background(myColors.surface)
                .background(
                    Brush.linearGradient(
                        myColors.primaryGradientColors.map {
                            it / 0.25f
                        }
                    )
                )
                .clickable(onClick = onClick)
                .padding(BottomNavigationConstants.DEFAULT_ICON_PADDING.dp)
                .size(BottomNavigationConstants.DEFAULT_ICON_SIZE.dp)
        )
    }
}

@Composable
private fun BottonNavigationItem(
    icon: IconSource,
    contentDescription: StringSource,
    onClick: () -> Unit,
    modifier: Modifier,
    isSelected: Boolean,
) {
    Box(modifier) {
        MyIcon(
            icon = icon,
            contentDescription = contentDescription.rememberString(),
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(BottomNavigationConstants.DEFAULT_ICON_PADDING.dp)
                .size(BottomNavigationConstants.DEFAULT_ICON_SIZE.dp)
        )
        BottomNavigationSelectedIndicator(isSelected)
    }
}

@Composable
fun BoxScope.BottomNavigationSelectedIndicator(
    isSelected: Boolean,
) {
    if (isSelected) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = myColors.primaryGradientColors.map { it / 0.15f }
                    )
                )
        )
        Box(
            Modifier
                .matchParentSize()
                .wrapContentHeight(Alignment.Bottom)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        myColors.primaryGradientColors
                    )
                )
        )
    }
}

@Composable
private fun SortIndicator(
    sort: Sort<DownloadSortBy>,
    onClick: () -> Unit,
    modifier: Modifier,
    isSelected: Boolean,
) {
    val totalIcon = BottomNavigationConstants.DEFAULT_ICON_SIZE
    val iconSize = (totalIcon * 0.7)
    val sortDirectionSize = totalIcon - iconSize
    Box(
        modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier
                .padding(
                    vertical = BottomNavigationConstants.DEFAULT_ICON_PADDING.dp,
                    horizontal = (BottomNavigationConstants.DEFAULT_ICON_PADDING + (sortDirectionSize / 2)).dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val color = LocalContentColor.current
            val activeAlpha = color / 0.75f
            if (sort.isAscending()) {
                MyIcon(
                    MyIcons.sortUp,
                    null,
                    Modifier
                        .size(sortDirectionSize.dp),
                    tint = activeAlpha
                )
            }
            MyIcon(
                sort.cell.icon,
                null,
                Modifier.size(iconSize.dp),
            )
            if (sort.isDescending()) {
                MyIcon(
                    MyIcons.sortDown,
                    null,
                    Modifier
                        .size(sortDirectionSize.dp),
                    tint = activeAlpha
                )
            }
        }
        BottomNavigationSelectedIndicator(isSelected)
    }
}

private fun Modifier.changeStatusOnSwipe(
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
): Modifier {
    return pointerInput(Unit) {
        val threshold = 100f
        var drag = 0f
        detectHorizontalDragGestures(
            onDragEnd = {
                if (drag > threshold) {
                    goToPrevious()
                    drag = 0f
                } else if (drag < -threshold) {
                    goToNext()
                    drag = 0f
                }
            }
        ) { _, dragAmount ->
            drag += dragAmount
        }
    }
}
