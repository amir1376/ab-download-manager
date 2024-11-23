package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.windowUtil.moveSafe
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.abdownloadmanager.desktop.pages.addDownload.shared.*
import com.abdownloadmanager.desktop.pages.category.toCategoryImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Check
import com.abdownloadmanager.desktop.ui.icons.default.Clear
import com.abdownloadmanager.desktop.ui.icons.default.Clipboard
import com.abdownloadmanager.desktop.ui.icons.default.Lock
import com.abdownloadmanager.desktop.ui.icons.default.QuestionMark
import com.abdownloadmanager.desktop.ui.icons.default.Refresh
import com.abdownloadmanager.desktop.ui.icons.default.Settings
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.downloader.utils.OnDuplicateStrategy
import ir.amirab.util.compose.asStringSource
import java.awt.MouseInfo

@Composable
fun AddDownloadPage(
    component: AddSingleDownloadComponent,
) {
    val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
    Column(
        Modifier
            .padding(horizontal = 32.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        val credentials by component.credentials.collectAsState()
        fun setLink(link: String) {
            component.setCredentials(
                credentials.copy(link = link)
            )
        }

        val linkFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            component.onPageOpen()
            linkFocus.requestFocus()
        }
        HandleEffects(component) {
            when (it) {
                is AddSingleDownloadPageEffects.SuggestUrl -> {
                    setLink(it.link)
                }
            }
        }
        UrlTextField(
            text = credentials.link,
            setText = {
                setLink(it)
            },
            modifier = Modifier.focusRequester(linkFocus)
        )
        Row(
        ) {
            val canAddResult by component.canAddResult.collectAsState()
            Column(Modifier.weight(1f)) {
                val useCategory by component.useCategory.collectAsState()
                Spacer(Modifier.size(8.dp))
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .onClick {
                                component.setUseCategory(!useCategory)
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        CheckBox(
                            size = 16.dp,
                            value = useCategory,
                            onValueChange = { component.setUseCategory(it) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(myStringResource(Res.string.use_category))
                    }
                    Spacer(Modifier.width(8.dp))
                    CategorySelect(
                        modifier = Modifier.weight(1f),
                        enabled = useCategory,
                        categories = component.categories.collectAsState().value,
                        selectedCategory = component.selectedCategory.collectAsState().value,
                        onCategorySelected = {
                            component.setSelectedCategory(it)
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    CategoryAddButton(
                        enabled = useCategory,
                        modifier = Modifier.fillMaxHeight(),
                        onClick = {
                            component.addNewCategory()
                        },
                    )
                }
                Spacer(Modifier.size(8.dp))
                LocationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = component.folder.collectAsState().value,
                    setText = {
                        component.setFolder(it)
                    },
                    errorText = when (canAddResult) {
                        CanAddResult.CantWriteInThisFolder -> myStringResource(Res.string.cant_write_to_this_folder)
                        else -> null
                    },
                    lastUsedLocations = component.lastUsedLocations.collectAsState().value
                )
                val name by component.name.collectAsState()
                Spacer(Modifier.size(8.dp))
                NameTextField(
                    text = name,
                    setText = {
                        component.setName(it)
                    },
                    errorText = when (canAddResult) {
                        is CanAddResult.DownloadAlreadyExists -> {
                            if (onDuplicateStrategy == null) {
                                myStringResource(Res.string.file_name_already_exists)
                            } else {
                                null
                            }
                        }

                        CanAddResult.InvalidFileName -> myStringResource(Res.string.invalid_file_name)
                        else -> null
                    }.takeIf { name.isNotEmpty() }
                )
            }
            Spacer(Modifier.size(24.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Top)
                    .width(IntrinsicSize.Max)
            ) {
                RenderFileTypeAndSize(component)
                RenderResumeSupport(component)
                ConfigActionsButtons(component)
            }
        }
        Spacer(Modifier.weight(1f))
        MainActionButtons(component)
        if (component.showSolutionsOnDuplicateDownloadUi) {
            ShowSolutionsOnDuplicateDownload(component)
        }
        if (component.shouldShowAddToQueue) {
            ShowAddToQueueDialog(
                queueList = component.queues.collectAsState().value,
                onClose = { component.shouldShowAddToQueue = false },
                onQueueSelected = {
                    component.onRequestAddToQueue(it)
                }
            )
        }
        if (component.showMoreSettings) {
            ExtraConfig(
                onDismiss = { component.showMoreSettings = false },
                configurables = component.configurables,
            )
        }
    }
}

@Composable
private fun ShowSolutionsOnDuplicateDownload(component: AddSingleDownloadComponent) {
    val h = 250
    val w = 300
    val state = rememberDialogState(
        size = DpSize(
            height = Dp.Unspecified,
            width = Dp.Unspecified,
        ),
    )
    val close = {
        component.showSolutionsOnDuplicateDownloadUi = false
    }
    val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
    BaseOptionDialog(
        onCloseRequest = close,
        state = state,
        resizeable = false,
    ) {
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


        val shape = RoundedCornerShape(6.dp)
        Column(
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
            WithContentColor(myColors.onBackground) {
                Column(
                    Modifier.widthIn(max = 300.dp)
                ) {
                    WindowDraggableArea(Modifier) {
                        Column(
                            Modifier.padding(16.dp)
                        ) {
                            Text(
                                myStringResource(Res.string.select_a_solution),
                                Modifier,
                                fontSize = myTextSizes.base
                            )
                            Spacer(Modifier.height(8.dp))
                            WithContentAlpha(0.75f) {
                                Text(
                                    myStringResource(Res.string.select_download_strategy_description),
                                    Modifier,
                                    fontSize = myTextSizes.sm,
                                )
                            }
                        }
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Divider()
                        Spacer(Modifier.height(4.dp))
                        Column {
                            OnDuplicateStrategySolutionItem(
                                isSelected = onDuplicateStrategy == OnDuplicateStrategy.AddNumbered,
                                title = myStringResource(Res.string.download_strategy_add_a_numbered_file),
                                description = myStringResource(Res.string.download_strategy_add_a_numbered_file_description),
                            ) {
                                component.setOnDuplicateStrategy(OnDuplicateStrategy.AddNumbered)
                                close()
                            }
                            OnDuplicateStrategySolutionItem(
                                isSelected = onDuplicateStrategy == OnDuplicateStrategy.OverrideDownload,
                                title = myStringResource(Res.string.download_strategy_override_existing_file),
                                description = myStringResource(Res.string.download_strategy_override_existing_file_description),
                            ) {
                                component.setOnDuplicateStrategy(OnDuplicateStrategy.OverrideDownload)
                                close()
                            }
                            OnDuplicateStrategySolutionItem(
                                isSelected = null,
                                title = myStringResource(Res.string.download_strategy_show_downloaded_file),
                                description = myStringResource(Res.string.download_strategy_show_downloaded_file_description),
                            ) {
                                component.openDownloadFileForCurrentLink()
                                close()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnDuplicateStrategySolutionItem(
    title: String,
    description: String,
    isSelected: Boolean?,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        isSelected?.let {
            CheckBox(isSelected, { onClick() }, size = 12.dp)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                title,
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            WithContentAlpha(0.7f) {
                Text(
                    text = description,
                    fontSize = myTextSizes.sm,
                    modifier = Modifier
                )
            }
        }

    }
}


@Composable
private fun Divider() {
    Spacer(
        Modifier.fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10),
    )
}


@Composable
fun RenderResumeSupport(component: AddSingleDownloadComponent) {
    val fileInfo by component.linkResponseInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(16.dp)

    ) {
        val lineModifier = Modifier.weight(1f)
            .height(1.dp)
            .background(myColors.onBackground / 10)
        Box(lineModifier)
        val canAddToDownloads by component.canAddToDownloads.collectAsState()
        AnimatedVisibility(
            visible = canAddToDownloads && fileInfo != null,
        ) {
            fileInfo?.let { fileInfo ->
                if (fileInfo.resumeSupport) {
                    val iconModifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(10.dp)
                    if (fileInfo.resumeSupport) {
                        Icon(
                            imageVector = AbIcons.Default.Check,
                            contentDescription = null,
                            modifier = iconModifier,
                            tint = myColors.success
                        )
                    } else {
                        Icon(
                            imageVector = AbIcons.Default.Clear,
                            contentDescription = null,
                            modifier = iconModifier,
                            tint = myColors.error,
                        )
                    }
                }
            }
        }
        Box(lineModifier)


    }
}

@Composable
private fun MainConfigActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ActionButton(text, modifier, enabled, onClick)
}


@Composable
private fun PrimaryMainConfigActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 30
        }
    )
    val borderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors
    )
    val disabledBorderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 50
        }
    )
    ActionButton(
        text = text,
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        backgroundColor = backgroundColor,
        disabledBackgroundColor = backgroundColor,
        borderColor = borderColor,
        disabledBorderColor = disabledBorderColor,
    )
}

@Composable
fun ConfigActionsButtons(component: AddSingleDownloadComponent) {
    val responseInfo by component.linkResponseInfo.collectAsState()
    Row {
        IconActionButton(
            icon = AbIcons.Default.Refresh,
            contentDescription = myStringResource(Res.string.refresh)
        ) {
            component.refresh()
        }
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            icon = AbIcons.Default.Settings,
            contentDescription = myStringResource(Res.string.settings),
            indicateActive = component.showMoreSettings,
            requiresAttention = responseInfo?.requireBasicAuth ?: false
        ) {
            component.showMoreSettings = true
        }
    }
}

@Composable
private fun MainActionButtons(component: AddSingleDownloadComponent) {
    Row {
        val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
        val canAddResult by component.canAddResult.collectAsState()
        if (canAddResult is CanAddResult.DownloadAlreadyExists && onDuplicateStrategy == null) {
            MainConfigActionButton(
                text = myStringResource(Res.string.show_solutions),
                modifier = Modifier,
                onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
            )
            if (component.shouldShowOpenFile.collectAsState().value) {
                Spacer(Modifier.width(8.dp))
                MainConfigActionButton(
                    text = myStringResource(Res.string.open_file),
                    modifier = Modifier,
                    onClick = { component.openExistingFile() },
                )
            }
        } else {
            val canAddToDownloads by component.canAddToDownloads.collectAsState()
            MainConfigActionButton(
                text = myStringResource(Res.string.add),
                modifier = Modifier,
                enabled = canAddToDownloads,
                onClick = {
                    component.shouldShowAddToQueue = true
                },
            )
            Spacer(Modifier.width(8.dp))
            PrimaryMainConfigActionButton(
                text = myStringResource(Res.string.download),
                modifier = Modifier,
                enabled = canAddToDownloads,
                onClick = {
                    component.onRequestDownload()
                },
            )
            if (onDuplicateStrategy != null) {
                Spacer(Modifier.width(8.dp))
                MainConfigActionButton(
                    text = myStringResource(Res.string.change_solution),
                    modifier = Modifier,
                    onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
                )
            }

        }
        //        Spacer(Modifier.weight(1f))
        Spacer(Modifier.weight(1f))

        MainConfigActionButton(
            text = myStringResource(Res.string.cancel),
            modifier = Modifier,
            onClick = {
                component.onRequestClose()
            },
        )
    }
}

@Composable
fun RenderFileTypeAndSize(
    component: AddSingleDownloadComponent,
) {
    val isLinkLoading by component.isLinkLoading.collectAsState()
    val fileInfo by component.linkResponseInfo.collectAsState()
    val fileIconProvider = component.iconProvider
    val iconModifier = Modifier.size(16.dp)
    Box(Modifier.padding(top = 16.dp)) {
        AnimatedContent(
            targetState = isLinkLoading,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { loading ->
            if (loading) {
                LoadingIndicator(iconModifier)
            } else {
//                val extension = getExtension(fileInfo?.fileName ?: usersSetFileName) ?: "unknown"
                val downloadItem by component.downloadItem.collectAsState()
                val icon = fileIconProvider.rememberCategoryIcon(downloadItem.name).toCategoryImageVector()

//                val bitmap = FileIconProvider.getIconOfFileExtension(extension)

                AnimatedContent(
                    fileInfo,
                ) { fileInfo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WithContentAlpha(1f) {
                            if (fileInfo != null) {
                                if (fileInfo.requiresAuth) {
                                    Icon(
                                        imageVector = AbIcons.Default.Lock,
                                        contentDescription = null,
                                        modifier = iconModifier,
                                        tint = myColors.error
                                    )
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = iconModifier
                                )
                                val size = fileInfo.totalLength?.let {
                                    convertSizeToHumanReadable(it)
                                }.takeIf {
                                    // this is a length of a html page (error)
                                    fileInfo.isSuccessFul
                                } ?: Res.string.unknown.asStringSource()
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    size.rememberString(),
                                    fontSize = myTextSizes.sm,
                                )
                            } else {
                                Icon(
                                    imageVector = AbIcons.Default.QuestionMark,
                                    contentDescription = null,
                                    modifier = iconModifier,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getExtension(s: String): String? {
    if (s.isBlank()) return null
    return s.substringAfterLast(".", "")
        .takeIf { it.isNotBlank() }
}

@Composable
fun MyTextFieldIcon(
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Default)
            .fillMaxHeight()
            .clickable(enabled = enabled, onClick = onClick)
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
            .size(16.dp)
    )
}

@Composable
private fun UrlTextField(
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    AddDownloadPageTextField(
        text,
        setText,
        myStringResource(Res.string.download_link),
        modifier = modifier.fillMaxWidth(),
        end = {
            MyTextFieldIcon(AbIcons.Default.Clipboard) {
                setText(
                    ClipboardUtil.read()
                        .orEmpty()
                )
            }
        },
        errorText = errorText
    )
}

@Composable
private fun NameTextField(
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
) {
    AddDownloadPageTextField(
        text,
        setText,
        myStringResource(Res.string.name),
        modifier = Modifier.fillMaxWidth(),
        errorText = errorText,
    )
}

@Composable
fun AddDownloadPageTextField(
    text: String,
    setText: (String) -> Unit,
    placeHolder: String,
    modifier: Modifier,
    errorText: String? = null,
    start: @Composable() (() -> Unit)? = null,
    end: @Composable() (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val dividerModifier = Modifier
        .fillMaxHeight()
        .padding(vertical = 1.dp)
        //to not conflict with text-field border
        .width(1.dp)
        .background(if (isFocused) myColors.onBackground / 10 else Color.Transparent)
    Column(modifier) {
        MyTextField(
            text,
            setText,
            placeHolder,
            modifier = Modifier.fillMaxWidth(),
            background = myColors.surface / 50,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(6.dp),
            start = start?.let {
                {
                    WithContentAlpha(0.5f) {
                        it()
                    }
                    Spacer(dividerModifier)
                }
            },
            end = end?.let {
                {
                    Spacer(dividerModifier)
                    it()
                }
            }
        )
        AnimatedVisibility(errorText != null) {
            if (errorText != null) {
                Text(
                    errorText,
                    Modifier.padding(bottom = 4.dp, start = 4.dp),
                    fontSize = myTextSizes.sm,
                    color = myColors.error,
                )
            }
        }
    }
}

