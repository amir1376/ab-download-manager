package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.desktop.pages.addDownload.shared.ExtraConfig
import com.abdownloadmanager.desktop.pages.addDownload.shared.LocationTextField
import com.abdownloadmanager.desktop.pages.addDownload.shared.ShowAddToQueueDialog
import com.abdownloadmanager.desktop.pages.home.sections.category.DefinedTypeCategories
import com.abdownloadmanager.desktop.ui.WithContentAlpha
import com.abdownloadmanager.desktop.ui.WithContentColor
import com.abdownloadmanager.desktop.ui.customwindow.BaseOptionDialog
import com.abdownloadmanager.desktop.ui.icon.IconSource
import com.abdownloadmanager.desktop.ui.icon.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.utils.OnDuplicateStrategy
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
                Spacer(Modifier.size(8.dp))
                LocationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    text = component.folder.collectAsState().value,
                    setText = {
                        component.setFolder(it)
                    },
                    errorText = when (canAddResult) {
                        CanAddResult.CantWriteInThisFolder -> "Can't write to this folder"
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
                                "File name already exists"
                            } else {
                                null
                            }
                        }

                        CanAddResult.InvalidFileName -> "Invalid filename"
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
            ExtraConfig(component)
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
                                "Select a solution",
                                Modifier,
                                fontSize = myTextSizes.base
                            )
                            Spacer(Modifier.height(8.dp))
                            WithContentAlpha(0.75f) {
                                Text(
                                    "The link you provided is already in download lists please specify what you want to do",
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
                                title = "Add a numbered file",
                                description = "Add an index after the end of download file name",
                            ) {
                                component.setOnDuplicateStrategy(OnDuplicateStrategy.AddNumbered)
                                close()
                            }
                            OnDuplicateStrategySolutionItem(
                                isSelected = onDuplicateStrategy == OnDuplicateStrategy.OverrideDownload,
                                title = "Override existing file",
                                description = "Remove existing download and write to that file",
                            ) {
                                component.setOnDuplicateStrategy(OnDuplicateStrategy.OverrideDownload)
                                close()
                            }
                            OnDuplicateStrategySolutionItem(
                                isSelected = null,
                                title = "Show downloaded file",
                                description = "Show already existing download item , so you can press on resume or open it",
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
            Text(title,
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
                        MyIcon(
                            icon = MyIcons.check,
                            contentDescription = null,
                            modifier = iconModifier,
                            tint = myColors.success
                        )
                    } else {
                        MyIcon(
                            icon = MyIcons.clear,
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
        IconActionButton(MyIcons.refresh, "Refresh") {
            component.refresh()
        }
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            MyIcons.settings,
            "Settings",
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
                text = "Show solutions...",
                modifier = Modifier,
                onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
            )
        } else {
            val canAddToDownloads by component.canAddToDownloads.collectAsState()
            MainConfigActionButton(
                text = "Add",
                modifier = Modifier,
                enabled = canAddToDownloads,
                onClick = {
                    component.shouldShowAddToQueue = true
                },
            )
            Spacer(Modifier.width(8.dp))
            PrimaryMainConfigActionButton(
                text = "Download",
                modifier = Modifier,
                enabled = canAddToDownloads,
                onClick = {
                    component.onRequestDownload()
                },
            )
            if (onDuplicateStrategy != null) {
                Spacer(Modifier.width(8.dp))
                MainConfigActionButton(
                    text = "Change solution",
                    modifier = Modifier,
                    onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
                )
            }

        }
        //        Spacer(Modifier.weight(1f))
        Spacer(Modifier.weight(1f))

        MainConfigActionButton(
            text = "Cancel",
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
                val category = remember(downloadItem) {
                    DefinedTypeCategories.resolveCategoryForDownloadItem(
                        ProcessingDownloadItemState.onlyDownloadItem(downloadItem)
                    )
                }

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
                                    MyIcon(
                                        MyIcons.lock,
                                        null,
                                        iconModifier,
                                        tint = myColors.error
                                    )
                                }
                                MyIcon(
                                    category.icon,
                                    null,
                                    iconModifier
                                )
                                val size = fileInfo.totalLength?.let {
                                    convertSizeToHumanReadable(it)
                                }.takeIf {
                                    // this is a length of a html page (error)
                                    fileInfo.isSuccessFul
                                } ?: "unknown"
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    size,
                                    fontSize = myTextSizes.sm,
                                )
                            } else {
                                MyIcon(
                                    icon = MyIcons.question,
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
    icon: IconSource,
    onClick: () -> Unit,
) {
    MyIcon(icon, null, Modifier
        .pointerHoverIcon(PointerIcon.Default)
        .fillMaxHeight()
        .clickable { onClick() }
        .wrapContentHeight()
        .padding(horizontal = 8.dp)
        .size(16.dp))
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
        "Download link",
        modifier = modifier.fillMaxWidth(),
        end = {
            MyTextFieldIcon(MyIcons.paste) {
                setText(ClipboardUtil.read()
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
    errorText: String? = null
) {
    AddDownloadPageTextField(
        text,
        setText,
        "Name",
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

