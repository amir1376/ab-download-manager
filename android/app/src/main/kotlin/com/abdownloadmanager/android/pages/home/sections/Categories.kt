package com.abdownloadmanager.android.pages.home.sections

import androidx.compose.animation.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ExpandableItem
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.abdownloadmanager.android.pages.home.HomeComponent
import com.abdownloadmanager.android.ui.menu.RenderMenuInSinglePage
import com.abdownloadmanager.android.ui.myCombinedClickable
import com.abdownloadmanager.shared.pages.home.category.DefinedStatusCategories
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.pages.home.category.DownloadStatusCategoryFilter
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.rememberIconPainter
import com.abdownloadmanager.shared.util.ui.theme.myShapes

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.CategoryActions
import com.abdownloadmanager.shared.ui.widget.rememberMyPopupPositionProviderAtPosition
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource

@Composable
fun Categories(
    component: HomeComponent,
    modifier: Modifier,
) {
    val filterMode = component.filterMode.value
    val currentStatusFilter = (filterMode as? HomeComponent.FilterMode.Status)?.downloadStatus
    val currentTypeFilter = (filterMode as? HomeComponent.FilterMode.Status)?.category
    val categories by component.categoryManager.categoriesFlow.collectAsState()
    val clipShape = myShapes.defaultRounded
    val showCategoryOption by component.categoryActions.collectAsState()
    var popupOffset by remember { mutableStateOf(Offset.Zero) }
    fun showCategoryOption(item: Category?, offset: Offset) {
        popupOffset = offset
        component.showCategoryOptions(item)
    }

    fun closeCategoryOptions() {
        component.closeCategoryOptions()
    }
    Column(
        modifier
            .clip(clipShape)
            .border(1.dp, myColors.surface, clipShape)
            .padding(1.dp)
    ) {
        var expendedItem: DownloadStatusCategoryFilter? by remember {
            mutableStateOf(currentStatusFilter.takeIf { currentTypeFilter != null })
        }
        for (statusCategoryFilter in DefinedStatusCategories.values()) {
            StatusFilterItem(
                isExpanded = expendedItem == statusCategoryFilter,
                currentTypeCategoryFilter = currentTypeFilter,
                currentStatusCategoryFilter = currentStatusFilter,
                statusFilter = statusCategoryFilter,
                categories = categories,
                onFilterChange = {
                    component.onCategoryFilterChange(statusCategoryFilter, it)
                },
                onRequestExpand = { expand ->
                    expendedItem = statusCategoryFilter.takeIf { expand }
                },
                onItemsDroppedInCategory = { category, ids ->
                    component.moveItemsToCategory(category, ids)
                },
                onRequestOpenOptionMenu = { category, offset ->
                    showCategoryOption(category, offset)
                }
            )
        }
    }
    showCategoryOption?.let {
        CategoryOption(
            categoryOptionMenuState = it,
            onDismiss = {
                closeCategoryOptions()
            },
            offset = popupOffset,
        )
    }
}

@Composable
fun CategoryOption(
    categoryOptionMenuState: CategoryActions,
    onDismiss: () -> Unit,
    offset: Offset,
) {
    ShowOptionsInPopupWithOffset(
        MenuItem.SubMenu(
            icon = categoryOptionMenuState.categoryItem?.rememberIconPainter(),
            title = categoryOptionMenuState.categoryItem?.name?.asStringSource()
                ?: Res.string.categories.asStringSource(),
            categoryOptionMenuState.menu,
        ),
        popupOffset = offset,
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun ShowOptionsInPopupWithOffset(
    menu: MenuItem.SubMenu,
    popupOffset: Offset,
    onDismissRequest: () -> Unit
) {
    Popup(
        popupPositionProvider = rememberMyPopupPositionProviderAtPosition(
            popupOffset
        ),
        onDismissRequest = onDismissRequest
    ) {
        RenderMenuInSinglePage(
            menu, onDismissRequest,
            Modifier.width(IntrinsicSize.Max),
        )
    }
}


@Composable
private fun CategoryFilterItem(
    modifier: Modifier,
    category: Category,
    isSelected: Boolean,
    onItemsDropped: (ids: List<Long>) -> Unit,
    onClick: () -> Unit,
    onRequestOpenOptionMenu: (Category?, Offset) -> Unit,
) {
//    var isDraggingOnMe by remember { mutableStateOf(false) }
    var layoutCoordinates by remember {
        mutableStateOf<LayoutCoordinates?>(null)
    }
    Box(
        modifier
//            .dropDownloadItemsHere(
//                onDragIn = { isDraggingOnMe = true },
//                onDragDone = { isDraggingOnMe = false },
//                onItemsDropped = onItemsDropped,
//            )
            .background(
                if (isSelected) {
                    myColors.onBackground / 0.05f
                } else Color.Transparent
            )
//            .ifThen(isDraggingOnMe) {
//                val infiniteTransition = rememberInfiniteTransition()
//                val color by infiniteTransition.animateColor(
//                    initialValue = myColors.primary,
//                    targetValue = myColors.secondary,
//                    animationSpec = infiniteRepeatable(
//                        animation = tween(1000, easing = LinearEasing),
//                        repeatMode = RepeatMode.Reverse
//                    )
//                )
//                border(1.dp, color)
//            }
            .heightIn(mySpacings.thumbSize)
            .onGloballyPositioned {
                layoutCoordinates = it
            }
            .myCombinedClickable(
                onLongClick = {
                    onRequestOpenOptionMenu(
                        category,
                        layoutCoordinates?.localToWindow(it)
                            ?: Offset.Zero
                    )
                },
                onClick = {
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
//        if (isDraggingOnMe) {
//            DelayedTooltipPopup(
//                {},
//                myStringResource(Res.string.move_to_this_category),
//            )
//        }
        Row(
            modifier = Modifier
                .padding(start = 24.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                val iconPainter = category.rememberIconPainter()
                MyIcon(
                    iconPainter ?: MyIcons.folder,
                    null,
                    Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    category.name,
                    Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = myTextSizes.base
                )
            }
        }
        AnimatedVisibility(
            isSelected,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = scaleIn(),
            exit = scaleOut(),
        ) {
            Spacer(
                Modifier
                    .height(16.dp)
                    .width(3.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 12.dp,
                            topEnd = 12.dp,
                        )
                    )
                    .background(myColors.primary)
            )
        }
    }
}

@Composable
fun StatusFilterItem(
    isExpanded: Boolean,
    onRequestExpand: (Boolean) -> Unit,
    currentTypeCategoryFilter: Category?,
    currentStatusCategoryFilter: DownloadStatusCategoryFilter?,
    statusFilter: DownloadStatusCategoryFilter,
    categories: List<Category>,
    onItemsDroppedInCategory: (category: Category, downloadIds: List<Long>) -> Unit,
    onFilterChange: (
        typeFilter: Category?,
    ) -> Unit,
    onRequestOpenOptionMenu: (Category?, Offset) -> Unit,
) {
    val isStatusSelected = currentStatusCategoryFilter == statusFilter
    val isSelected = isStatusSelected && currentTypeCategoryFilter == null
    var layoutCoordinates by remember {
        mutableStateOf<LayoutCoordinates?>(null)
    }
    ExpandableItem(
        modifier = Modifier,
        isExpanded = isExpanded,
        header = {
            Box(
                Modifier
                    .height(IntrinsicSize.Max)
                    .heightIn(mySpacings.thumbSize)
                    .background(
                        if (isSelected) {
                            myColors.onBackground / 0.05f
                        } else Color.Transparent
                    )
                    .onGloballyPositioned {
                        layoutCoordinates = it
                    }
                    .myCombinedClickable(
                        onClick = {
                            if (!isExpanded) {
                                onRequestExpand(true)
                            }
                            onFilterChange(null)
                        },
                        onLongClick = {
                            onRequestOpenOptionMenu(
                                null,
                                layoutCoordinates?.localToWindow(it) ?: Offset.Zero
                            )
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    )
            ) {
                Row(
                    Modifier
                        .padding(vertical = 4.dp)
                        .padding(start = 16.dp)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WithContentAlpha(if (isSelected) 1f else 0.75f) {
                        MyIcon(
                            statusFilter.icon,
                            null,
                            Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            statusFilter.name.rememberString(),
                            Modifier.weight(1f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = myTextSizes.lg,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                        MyIcon(
                            MyIcons.up, null, Modifier
                                .fillMaxHeight()
                                .wrapContentHeight()
                                .clip(CircleShape)
                                .clickable {
                                    onRequestExpand(!isExpanded)
                                }
                                .padding(6.dp)
                                .size(16.dp)
                                .rotate(if (isExpanded) 0f else 180f)
                        )
                    }
                }
                AnimatedVisibility(
                    isSelected,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    Spacer(
                        Modifier
                            .height(16.dp)
                            .width(3.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 0.dp,
                                    bottomEnd = 12.dp,
                                    topEnd = 12.dp,
                                )
                            )
                            .background(myColors.primary)
                    )
                }
            }
        },
        body = {
            Column(Modifier) {
                categories.forEach { category ->
                    key(category.id) {
                        CategoryFilterItem(
                            modifier = Modifier,
                            category = category,
                            isSelected = isStatusSelected && currentTypeCategoryFilter == category,
                            onItemsDropped = {
                                onItemsDroppedInCategory(category, it)
                            },
                            onClick = {
                                onFilterChange(category)
                            },
                            onRequestOpenOptionMenu = onRequestOpenOptionMenu,
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    )
}

