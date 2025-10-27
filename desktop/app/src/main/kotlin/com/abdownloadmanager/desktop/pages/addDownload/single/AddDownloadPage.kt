package com.abdownloadmanager.desktop.pages.addDownload.single

import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.window.moveSafe
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import arrow.core.Some
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.desktop.pages.addDownload.shared.*
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.add.CanAddResult
import com.abdownloadmanager.shared.pages.adddownload.single.BaseAddSingleDownloadComponent
import com.abdownloadmanager.shared.pages.adddownload.single.AddSingleDownloadPageEffects
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.downloader.utils.OnDuplicateStrategy
import java.awt.MouseInfo

@Composable
fun AddDownloadPage(
    component: BaseAddSingleDownloadComponent,
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
                credentials.copy(link = Some(link))
            )
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
            modifier = Modifier
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
                    lastUsedLocations = component.lastUsedLocations.collectAsState().value,
                    onRequestRemoveSaveLocation = component::removeFromLastDownloadLocation,
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
                                myStringResource(Res.string.download_already_exists)
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
                onQueueSelected = { queue, startQueue ->
                    component.onRequestAddToQueue(queue, startQueue)
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
private fun ShowSolutionsOnDuplicateDownload(component: BaseAddSingleDownloadComponent) {
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


        val shape = myShapes.defaultRounded
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
                            Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
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
                                title = myStringResource(Res.string.download_strategy_update_download_link),
                                description = myStringResource(Res.string.download_strategy_update_download_link_description),
                            ) {
                                component.updateDownloadCredentialsOfOriginalDownload()
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
fun RenderResumeSupport(component: BaseAddSingleDownloadComponent) {
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
fun ConfigActionsButtons(component: BaseAddSingleDownloadComponent) {
    val responseInfo by component.linkResponseInfo.collectAsState()
    Row {
        IconActionButton(MyIcons.refresh, myStringResource(Res.string.refresh)) {
            component.refresh()
        }
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            MyIcons.settings,
            myStringResource(Res.string.settings),
            indicateActive = component.showMoreSettings,
            requiresAttention = responseInfo?.requireBasicAuth ?: false
        ) {
            component.showMoreSettings = true
        }
    }
}

@Composable
private fun MainActionButtons(component: BaseAddSingleDownloadComponent) {
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
            PrimaryMainActionButton(
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
    component: BaseAddSingleDownloadComponent,
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
                val icon = fileIconProvider.rememberIcon(downloadItem.name)

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
                                    icon,
                                    null,
                                    iconModifier
                                )
                                val size = component.getLengthString()
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    size.rememberString(),
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
private fun UrlTextField(
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    MyTextFieldWithIcons(
        text,
        setText,
        myStringResource(Res.string.download_link),
        modifier = modifier.fillMaxWidth(),
        end = {
            MyTextFieldIcon(MyIcons.paste) {
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
    MyTextFieldWithIcons(
        text,
        setText,
        myStringResource(Res.string.name),
        modifier = Modifier.fillMaxWidth(),
        errorText = errorText,
    )
}

