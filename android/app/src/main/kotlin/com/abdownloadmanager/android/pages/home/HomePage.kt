package com.abdownloadmanager.android.pages.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.abdownloadmanager.android.pages.enterurl.EnterNewURLPage
import com.abdownloadmanager.android.pages.home.sections.sort.RenderSortMenu
import com.abdownloadmanager.android.ui.menu.RenderMenuInSinglePage
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.android.util.AndroidIntentUtils
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.BaseHomeComponent
import com.abdownloadmanager.shared.pages.home.CategoryDeletePromptState
import com.abdownloadmanager.shared.pages.home.ConfirmPromptState
import com.abdownloadmanager.shared.pages.home.DeletePromptState
import com.abdownloadmanager.shared.ui.widget.rememberMyComponentCustomRectPositionProvider
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.modifiers.silentClickable
import ir.amirab.util.compose.resources.myStringResource
import kotlinx.coroutines.launch


@Composable
fun HomePage(component: HomeComponent) {
    val selectionList by component.selectionList.collectAsState()
    val density = LocalDensity.current
    var contentPaddingValues by remember {
        mutableStateOf(PaddingValues.Zero)
    }
    val topPaddingInDp = contentPaddingValues.calculateTopPadding()
    val bottomPaddingInDp = contentPaddingValues.calculateBottomPadding()
    var showDeletePromptState by remember {
        mutableStateOf(null as DeletePromptState?)
    }
    var showDeleteCategoryPromptState by remember {
        mutableStateOf(null as CategoryDeletePromptState?)
    }
    var showConfirmPrompt by remember {
        mutableStateOf(null as ConfirmPromptState?)
    }
    val lazyListState = rememberLazyListState()
    val downloadList by component.sortedDownloadList.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    HandleEffects(component) { effect ->
        when (effect) {
            is BaseHomeComponent.Effects.Common -> {
                when (effect) {
                    is BaseHomeComponent.Effects.Common.DeleteItems -> {
                        if (effect.list.isNotEmpty()) {
                            showDeletePromptState = DeletePromptState(
                                downloadList = effect.list,
                                finishedCount = effect.finishedCount,
                                unfinishedCount = effect.unfinishedCount,
                            )
                        }
                    }

                    is BaseHomeComponent.Effects.Common.DeleteCategory -> {
                        showDeleteCategoryPromptState = CategoryDeletePromptState(effect.category)
                    }

                    is BaseHomeComponent.Effects.Common.AutoCategorize -> {
                        showConfirmPrompt = ConfirmPromptState(
                            title = Res.string.confirm_auto_categorize_downloads_title.asStringSource(),
                            description = Res.string.confirm_auto_categorize_downloads_description.asStringSource(),
                            onConfirm = component::onConfirmAutoCategorize
                        )
                    }

                    is BaseHomeComponent.Effects.Common.ResetCategoriesToDefault -> {
                        showConfirmPrompt = ConfirmPromptState(
                            title = Res.string.confirm_reset_to_default_categories_title.asStringSource(),
                            description = Res.string.confirm_reset_to_default_categories_description.asStringSource(),
                            onConfirm = component::onConfirmResetCategories
                        )
                    }

                    is BaseHomeComponent.Effects.Common.ScrollToDownloadItem -> {
                        val id = effect.downloadId
                        val positionOrNull = downloadList
                            .indexOfFirst { it.id == id }
                            .takeIf { it != -1 }
                        positionOrNull?.let {
                            coroutineScope.launch {
                                lazyListState.scrollToItem(it)
                            }
                        }
                    }
                }
            }

            is HomeComponent.Effects -> {
                when (effect) {
                    is HomeComponent.Effects.ShareFiles -> {
                        AndroidIntentUtils.shareFiles(context, effect.files)
                    }
                }
            }
            else -> {}
        }
    }
    val isOverlayVisible by component.isOverlayVisible.collectAsState()
    PageUi(
        header = {
            val headerAlpha = rememberHeaderAlpha(
                lazyListState,
                density.run {
                    topPaddingInDp.toPx()
                },
            ).value * 0.75f
            val colors = myColors
            PageHeader(
                modifier = Modifier
                    .background(
                        myColors.background.copy(
                            alpha = headerAlpha
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = mySpacings.largeSpace),
                leadingIcon = {
                    MyIcon(
                        MyIcons.appIcon,
                        null,
                        Modifier.size(mySpacings.iconSize),
                    )
                },
                headerTitle = {
                    PageTitle(
                        myStringResource(Res.string.app_title)
                    )
                }
            )
        },
        footer = {
            Footer(
                Modifier,
                component,
            )
        }
    ) {
        contentPaddingValues = it.paddingValues
        Box {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(myColors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DownloadList(
                    downloadList = downloadList,
                    selectionList = selectionList,
                    onItemSelectionChange = { id, checked ->
                        component.onItemSelectionChange(id, checked)
                    },
                    onItemClicked = {
                        component.onItemClicked(it)
                    },
                    fileIconProvider = component.fileIconProvider,
                    onNewSelection = {
                        component.newSelection(ids = it)
                    },
                    lazyListState = lazyListState,
                    modifier = Modifier
                        .weight(1f),
                    contentPadding = PaddingValues(
                        bottom = bottomPaddingInDp,
                        top = topPaddingInDp,
                    ),
                )
            }
            AnimatedVisibility(
                isOverlayVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                        )
                        .silentClickable {
                            component.onOverlayClicked()
                        }
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(bottomPaddingInDp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                myColors.background,
                            )
                        )
                    )
            )
            RenderAboveBottonNavigation(
                component, Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = bottomPaddingInDp)
            )
        }
    }

    val enterNewURLComponent = component.enterNewLinkSlot.rememberChild()
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(enterNewURLComponent) {
        if (enterNewURLComponent == null) {
            state.hide()
        } else {
            state.show()
        }
    }
    state.OnFullyDismissed {
        component.closeEnterNewURLWindow()
    }
    val onDismissEnterNewURLComponent = {
        state.hide()
    }
    ResponsiveDialog(
        state = state,
        onDismiss = onDismissEnterNewURLComponent
    ) {
        enterNewURLComponent?.let {
            EnterNewURLPage(it, onDismissEnterNewURLComponent)
        }
    }
    RenderPrompts(
        component = component,
        showDeletePromptState = showDeletePromptState,
        showDeleteCategoryPrompt = showDeleteCategoryPromptState,
        showConfirmPrompt = showConfirmPrompt,
        closeConfirmPrompt = {
            showConfirmPrompt = null
        },
        closeDeleteCategoryPrompt = {
            showDeleteCategoryPromptState = null
        },
        closeDeletePrompt = {
            showDeletePromptState = null
        },
    )
}

@Composable
fun Footer(
    modifier: Modifier,
    component: HomeComponent,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val selectionList by component.selectionList.collectAsState()
        AnimatedContent(
            selectionList.isNotEmpty(),
            transitionSpec = {
                val enter = slideInVertically(tween()) { it } + fadeIn(tween())
                val exit = slideOutVertically(tween()) { it } + fadeOut(tween())
                enter togetherWith exit
            },
            modifier = Modifier
        ) { hasSelection ->
            val commonModifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
            if (hasSelection) {
                RenderDownloadOptions(
                    modifier = commonModifier,
                    component = component,
                )
            } else {
                BottomNavigation(
                    commonModifier,
                    component,
                )
            }
        }
    }
}

@Composable
fun RenderAboveBottonNavigation(component: HomeComponent, modifier: Modifier) {
    val enter = fadeIn() + expandIn { IntSize(it.width, 0) }
    val exit = fadeOut() + shrinkOut { IntSize(it.width, 0) }
    RenderSortMenu(component, modifier, enter, exit)
    RenderStatusFilterMenu(component, modifier, enter, exit)
}

@Composable
private fun RenderDownloadOptions(
    modifier: Modifier,
    component: HomeComponent,
) {
    val selection by component.selectionList.collectAsState()
    val downloadList by component.sortedDownloadList.collectAsState()
    SelectionPopup(
        modifier = modifier,
        options = component.downloadActions.androidMenu,
        onRequestClose = component::clearSelection,
        renderSubMenu = { optionMenuProps, onRequestClose ->
            val state = rememberResponsiveDialogState(false)
            val onDismiss = {
                state.hide()
            }
            LaunchedEffect(optionMenuProps) {
                if (optionMenuProps == null) {
                    state.hide()
                } else {
                    state.show()
                }
            }
            state.OnFullyDismissed {
                onRequestClose()
            }
            optionMenuProps?.let {
                Popup(
                    popupPositionProvider = rememberMyComponentCustomRectPositionProvider(
                        providedAnchorBounds = it.layoutCoordinates.boundsInWindow().roundToIntRect(),
                        anchor = Alignment.TopEnd,
                        alignment = Alignment.TopStart,
                        offset = DpOffset(0.dp, (-4).dp)
                    ),
                    properties = PopupProperties(
                        focusable = true,
                        dismissOnClickOutside = true,
                    ),
                    onDismissRequest = onDismiss,
                ) {
                    RenderMenuInSinglePage(
                        optionMenuProps.subMenu,
                        onDismiss,
                        Modifier.width(IntrinsicSize.Max)
                    )
                }
            }
        },
        onRequestSelectAll = component::selectAll,
        onRequestSelectInside = component::onRequestSelectInside,
        onRequestInvertSelection = component::onRequestInvertSelection,
        selectionCount = selection.size,
        total = downloadList.size,
    )
}

private fun createAlphaForHeader(
    scrollOffset: Float,
    headerHeight: Float,
): Float {
    if (headerHeight == 0f) return 0f
    return (scrollOffset / headerHeight).coerceIn(0f..1f)
}

@Composable
fun rememberHeaderAlpha(
    listState: LazyListState,
    headerHeightPx: Float,
): State<Float> {
    val headerHeightPx by rememberUpdatedState(headerHeightPx)
    return remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                headerHeightPx == 0f -> 1f
                else -> {
                    val scrolled = listState.firstVisibleItemScrollOffset.toFloat()
                    (scrolled / headerHeightPx).coerceIn(0f, 1f)
                }
            }
        }
    }
}
