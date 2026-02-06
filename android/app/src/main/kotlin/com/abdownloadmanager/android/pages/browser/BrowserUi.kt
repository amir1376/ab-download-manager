package com.abdownloadmanager.android.pages.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.browser.bookmark.BookmarkList
import com.abdownloadmanager.android.pages.browser.bookmark.EditBookmarkSheet
import com.abdownloadmanager.android.storage.BrowserBookmark
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.android.ui.menu.RenderMenuInSheet
import com.abdownloadmanager.android.ui.page.PageFooter
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.android.ui.widget.LoadingState
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

@Composable
fun BrowserPage(
    browserComponent: BrowserComponent,
) {
    val scope = rememberCoroutineScope()
    val viewRegistry = remember {
        WebViewRegistry(scope, browserComponent)
    }
    DisposableEffect(viewRegistry) {
        onDispose {
            viewRegistry.disposeAll()
        }
    }
    val tabs by browserComponent.tabs.collectAsState()
    val tab = tabs.activeTab
    val tabWebViewHolder = remember(tab?.tabId) {
        tab?.let {
            viewRegistry.getWebViewHolder(it)
        }
    }
    BackHandler(tabs.tabsSize > 1) {
        tab?.let {
            browserComponent.closeTab(tab.tabId)
        }
    }
    BackHandler(tabWebViewHolder?.navigator?.canGoBack ?: false) {
        tabWebViewHolder?.webView?.goBack()
    }
    LaunchedEffect(tabs) {
        viewRegistry.onTabsUpdated(tabs)
    }
    PageUi(
        header = {
            PageHeader(
                leadingIcon = {
                    MyIcon(
                        MyIcons.earth,
                        null,
                        Modifier.size(mySpacings.iconSize)
                    )
                },
                headerTitle = {
                    PageTitle(
                        myStringResource(Res.string.browser)
                    )
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = mySpacings.largeSpace),
            )
        },
        footer = {
            PageFooter {
                Column(
                    Modifier
                        .background(myColors.surface)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Spacer(
                        Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(myColors.onSurface / 0.1f)
                    )
                    tab?.tabState?.loadingState?.let {
                        if (it is LoadingState.Loading) {
                            Box(
                                Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    Modifier
                                        .height(1.dp)
                                        .fillMaxWidth(it.progress)
                                        .background(myColors.info),
                                )
                            }
                        }
                    }
                    WithContentColor(myColors.onSurface) {
                        AddressBar(
                            browserComponent = browserComponent,
                            currentWebViewHolder = tabWebViewHolder,
                            tabs = tabs,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    ) {
        if (tabWebViewHolder != null) {
            ABDMWebView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(myColors.background)
                    .padding(it.paddingValues),
                webViewHolder = tabWebViewHolder,
            )
        } else {
            EmptyPage(
                Modifier
                    .fillMaxSize()
                    .background(myColors.background)
                    .padding(it.paddingValues),
                onRequestOpenUrlFromClipboard = {
                    ClipboardUtil.read()?.let {
                        browserComponent.newTab(
                            browserComponent.createNewUrlFor(it)
                        )
                    }
                },
                onRequestOpenBookmarks = {
                    browserComponent.setShowBookmarkList(true)
                }
            )
        }
    }
    RenderMenuInSheet(
        browserComponent.contextMenu.collectAsState().value,
        browserComponent::closeContextMenu
    )
    BookmarkList(
        visible = browserComponent.showBookmarkList.collectAsState().value,
        onDismissRequest = {
            browserComponent.setShowBookmarkList(false)
        },
        onRemoveBookmarkRequest = {
            browserComponent.removeBookmark(it.url)
        },
        onBookmarkClick = {
            browserComponent.setShowBookmarkList(false)
            val newLink = browserComponent.createNewUrlFor(it.url)
            tabWebViewHolder
                ?.navigator
                ?.loadUrl(newLink)
                ?: browserComponent.newTab(newLink)
        },
        bookmarks = browserComponent.bookmarks.collectAsState().value,
        onRequestEditBookmark = browserComponent::promptEditBookmark,
        onRequestNewBookmark = {
            browserComponent.promptAddBookmark((BrowserBookmark("", "")))
        },
    )
    val editBookmarkState by browserComponent.editBookmarkState.collectAsState()
    editBookmarkState?.let { s ->
        EditBookmarkSheet(
            state = s,
            onSave = {
                browserComponent.addToBookmarks(
                    it,
                    if (s.editMode) {
                        s.initialValue
                    } else {
                        null
                    },
                )
                browserComponent.dismissEditBookmark()
            },
            onCancel = {
                browserComponent.dismissEditBookmark()
            }
        )
    }
    RenderMenuInSheet(
        browserComponent.mainMenu.collectAsState().value,
        browserComponent::closeMainMenu,
    )
}

@Composable
fun EmptyPage(
    modifier: Modifier,
    onRequestOpenUrlFromClipboard: () -> Unit,
    onRequestOpenBookmarks: () -> Unit,
) {
    Box(modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                myStringResource(Res.string.browser_no_tab_open),
                maxLines = 1,
            )
            Spacer(Modifier.height(mySpacings.largeSpace))
            ActionButton(
                text = myStringResource(Res.string.browser_paste_and_go),
                onClick = onRequestOpenUrlFromClipboard,
                start = {
                    MyIcon(
                        MyIcons.paste,
                        null,
                        Modifier.size(mySpacings.iconSize)
                    )
                    Spacer(Modifier.width(mySpacings.mediumSpace))
                }
            )
            Spacer(Modifier.height(mySpacings.largeSpace))
            ActionButton(
                text = myStringResource(Res.string.browser_bookmarks),
                onClick = onRequestOpenBookmarks,
                start = {
                    MyIcon(
                        MyIcons.hearth,
                        null,
                        Modifier.size(mySpacings.iconSize)
                    )
                    Spacer(Modifier.width(mySpacings.mediumSpace))
                }
            )
        }
    }
}

@Composable
fun AddressBar(
    browserComponent: BrowserComponent,
    currentWebViewHolder: WebViewHolder?,
    tabs: ABDMTabs,
    modifier: Modifier,
) {
    val webViewState = currentWebViewHolder?.tab?.tabState
    val navigator = currentWebViewHolder?.navigator
    val canGoBack = navigator?.canGoBack ?: false
    val canGoForward = navigator?.canGoForward ?: false
    val currentURL = webViewState?.lastLoadedUrl
    val currentTitle = webViewState?.pageTitle
    var isTabListVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = mySpacings.mediumSpace)
            .padding(vertical = mySpacings.mediumSpace)
    ) {
        AddressField(
            currentPageURL = currentURL,
            currentPageTitle = currentTitle,
            currentPageIcon = remember(webViewState?.pageIcon) {
                webViewState?.pageIcon?.asImageBitmap()
            },
            onNewPageRequested = {
                it?.let { text ->
                    val newLink = browserComponent.createNewUrlFor(text)
                    navigator
                        ?.loadUrl(newLink)
                        ?: browserComponent.newTab(newLink)
                }
            }
        )
        Spacer(Modifier.height(mySpacings.mediumSpace))
        Row {
            TransparentIconActionButton(
                enabled = canGoBack,
                icon = MyIcons.back,
                contentDescription = Res.string.back.asStringSource()
            ) {
                navigator?.navigateBack()
            }
            TransparentIconActionButton(
                enabled = canGoForward,
                icon = MyIcons.next,
                contentDescription = Res.string.next.asStringSource()
            ) {
                navigator?.navigateForward()
            }
            Spacer(Modifier.width(16.dp))
            webViewState?.let {
                TransparentIconActionButton(
                    icon = if (webViewState.isLoading) {
                        MyIcons.close
                    } else {
                        MyIcons.refresh
                    },
                    contentDescription = Res.string.next.asStringSource()
                ) {
                    if (webViewState.isLoading) {
                        navigator?.stopLoading()
                    } else {
                        navigator?.reload()
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            val shape = myShapes.defaultRounded
            Box(
                Modifier
                    .sizeIn(mySpacings.thumbSize, mySpacings.thumbSize)
                    .clip(shape)
                    .border(
                        1.dp, myColors.onBackground / 0.1f, shape
                    )
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            isTabListVisible = !isTabListVisible
                        },
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${tabs.tabsSize}",
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                )
            }
            TransparentIconActionButton(
                MyIcons.menu,
                contentDescription = Res.string.menu.asStringSource()
            ) {
                browserComponent.openMainMenu()
            }
        }
    }
    TabList(
        visible = isTabListVisible,
        onDismissRequest = {
            isTabListVisible = false
        },
        onCloseTabRequest = {
            browserComponent.closeTab(it.tabId)
        },
        onTabClick = {
            isTabListVisible = false
            browserComponent.switchTab(it.tabId)
        },
        onRequestNewTab = { requestedUrl ->
            isTabListVisible = false
            browserComponent.newTab(
                requestedUrl?.let {
                    browserComponent.createNewUrlFor(it)
                }
            )
        },
        tabs = tabs,
        currentTabId = currentWebViewHolder?.tab?.tabId,
    )
}

@Composable
private fun TabList(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    tabs: ABDMTabs,
    onRequestNewTab: (String?) -> Unit,
    onTabClick: (ABDMBrowserTab) -> Unit,
    onCloseTabRequest: (ABDMBrowserTab) -> Unit,
    currentTabId: String?,
) {
    val responsiveState = rememberResponsiveDialogState(visible)
    LaunchedEffect(visible) {
        if (visible) {
            responsiveState.show()
        } else {
            responsiveState.hide()
        }
    }
    ResponsiveDialog(
        state = responsiveState,
        onDismiss = onDismissRequest
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            myStringResource(Res.string.browser_tabs),
                        )
                    },
                    headerActions = {
                        TransparentIconActionButton(
                            MyIcons.paste,
                            Res.string.paste.asStringSource(),
                        ) {
                            onRequestNewTab(
                                ClipboardUtil.read()
                            )
                        }
                        TransparentIconActionButton(
                            MyIcons.add,
                            Res.string.add.asStringSource(),
                        ) {
                            onRequestNewTab(null)
                        }
                        TransparentIconActionButton(
                            MyIcons.close,
                            Res.string.close.asStringSource(),
                        ) {
                            onDismissRequest()
                        }
                    }
                )
            }
        ) {
            LazyColumn {
                items(tabs.tabs) { tabItem ->
                    val isSelected = tabItem.tabId == currentTabId
                    Row(
                        modifier = Modifier
                            .heightIn(mySpacings.thumbSize)
                            .ifThen(isSelected) {
                                background(myColors.onBackground / 0.1f)
                            }
                            .clickable {
                                onTabClick(tabItem)
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val websiteIconBitmap = remember(tabItem.tabState.pageIcon) {
                            tabItem.tabState.pageIcon?.asImageBitmap()
                        }
                        val modifier = Modifier.size(24.dp)
                        if (websiteIconBitmap != null) {
                            Image(
                                bitmap = websiteIconBitmap,
                                contentDescription = null,
                                modifier = modifier,
                            )
                        } else {
                            MyIcon(
                                MyIcons.earth,
                                contentDescription = null,
                                modifier = modifier,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = tabItem.tabState.let {
                                it.pageTitle ?: it.lastLoadedUrl
                            }.orEmpty(),
                            modifier = Modifier
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        WithContentAlpha(0.5f) {
                            TransparentIconActionButton(
                                MyIcons.close,
                                Res.string.close.asStringSource(),
                            ) {
                                onCloseTabRequest(tabItem)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddressField(
    currentPageIcon: ImageBitmap?,
    currentPageURL: String?,
    currentPageTitle: String?,
    onNewPageRequested: (String?) -> Unit,
) {
    val title = currentPageTitle ?: currentPageURL ?: "Blank"
    val url = currentPageURL ?: ""
    val isSecure = remember(url) {
        url.startsWith("https://")
    }
    var isEditing by remember {
        mutableStateOf(false)
    }
    BackHandler(enabled = isEditing) {
        isEditing = false
    }
    val textFieldInteractionSource = remember { MutableInteractionSource() }
    val isFocused by textFieldInteractionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        isEditing = isFocused
    }
    if (isEditing) {
        val fr = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            fr.requestFocus()
        }
        var editingText by remember {
            mutableStateOf(url)
        }
        MyTextField(
            text = editingText,
            onTextChange = {
                editingText = it
            },
            interactionSource = textFieldInteractionSource,
            placeholder = "URL",
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(fr),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    isEditing = false
                    onNewPageRequested(editingText)
                },
            ),
            end = {
                MyIcon(
                    MyIcons.paste,
                    contentDescription = myStringResource(Res.string.paste),
                    modifier = Modifier
                        .clickable {
                            ClipboardUtil.read()?.let {
                                editingText = it
                            }
                        }
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp),
                )
                MyIcon(
                    MyIcons.clear,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            if (editingText.isNotEmpty()) {
                                editingText = ""
                            } else {
                                isEditing = false
                            }
                        }
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp),
                )
            },
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(mySpacings.thumbSize)
                .clip(myShapes.defaultRounded)
                .background(myColors.onSurface / 0.05f)
                .clickable {
                    isEditing = true
                }
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (currentPageIcon != null) {
                Image(
                    bitmap = currentPageIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(16.dp))
            }
            Text(
                title,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isSecure) {
                Spacer(Modifier.width(8.dp))
                MyIcon(
                    MyIcons.lock,
                    "HTTPS",
                    modifier = Modifier.size(24.dp),
                    tint = myColors.success,
                )
            }
        }
    }
}
