package com.abdownloadmanager.android.pages.add.single

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import arrow.core.Some
import com.abdownloadmanager.android.pages.add.shared.CategoryAddButton
import com.abdownloadmanager.android.pages.add.shared.CategorySelect
import com.abdownloadmanager.android.pages.add.shared.ExtraConfig
import com.abdownloadmanager.android.pages.add.shared.LocationTextField
import com.abdownloadmanager.android.pages.add.shared.ShowAddToQueueDialog
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetTitleWithDescription
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.resources.myStringResource


import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.downloaderinui.add.CanAddResult
import com.abdownloadmanager.shared.pages.adddownload.single.AddSingleDownloadPageEffects
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.downloader.utils.OnDuplicateStrategy

@Composable
fun ResponsiveDialogScope.AddSingleDownloadPage(
    component: AndroidAddSingleDownloadComponent,
    onDismiss: () -> Unit,
) {
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    SheetTitle(
                        myStringResource(Res.string.new_download)
                    )
                },
                headerActions = {
                    TransparentIconActionButton(
                        MyIcons.close,
                        contentDescription = myStringResource(Res.string.close),
                        onClick = onDismiss,
                    )
                }
            )
        }
    ) {
        val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
        Column(
            Modifier
                .padding(horizontal = mySpacings.mediumSpace)
        ) {
            Column(
                Modifier
                    .weight(1f, false)
                    .verticalScroll(rememberScrollState())
            ) {
                val credentials by component.credentials.collectAsState()
                fun setLink(link: String) {
                    component.setCredentials(
                        credentials.copy(link = Some(link))
                    )
                }

                val showMoreInputs by component.showMoreInputs.collectAsState()

                HandleEffects(component) {
                    when (it) {
                        is AddSingleDownloadPageEffects.SuggestUrl -> {
                            setLink(it.link)
                        }
                    }
                }

                val canAddResult by component.canAddResult.collectAsState()
                Column {
                    UrlTextField(
                        text = credentials.link,
                        setText = {
                            setLink(it)
                        },
                        modifier = Modifier
                    )
                    AnimatedVisibility(showMoreInputs) {
                        Column {
                            Space()
                            val useCategory by component.useCategory.collectAsState()
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            component.setUseCategory(!useCategory)
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    CheckBox(
                                        size = 16.dp,
                                        value = useCategory,
                                        onValueChange = { component.setUseCategory(it) }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(myStringResource(Res.string.use_category))
                                }
                                Space()
                                Row {
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
                                        modifier = Modifier,
                                        onClick = {
                                            component.addNewCategory()
                                        },
                                    )
                                }
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
                        }
                    }
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
            }
            Column {
                Space()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RenderFileTypeAndSize(component)
                    RenderResumeSupport(component, Modifier.weight(1f))
                    ConfigActionsButtons(component)
                }
                Space()
                MainActionButtons(component)
                ShowSolutionsOnDuplicateDownload(component)
                ShowAddToQueueDialog(
                    isOpened = component.shouldShowAddToQueue,
                    queueList = component.queues.collectAsState().value,
                    onClose = { component.shouldShowAddToQueue = false },
                    onQueueSelected = { queue, startQueue ->
                        component.onRequestAddToQueue(queue, startQueue)
                    },
                    newQueueAction = component.newQueuesAction
                )
                ExtraConfig(
                    isOpened = component.showMoreSettings,
                    onDismiss = { component.showMoreSettings = false },
                    configurables = component.configurables,
                )
            }
        }
    }

}

@Composable
private fun Space() {
    Spacer(Modifier.size(mySpacings.mediumSpace))
}

@Composable
private fun ShowSolutionsOnDuplicateDownload(
    component: AndroidAddSingleDownloadComponent,
) {
    val state = rememberResponsiveDialogState(false)
    val isOpen = component.showSolutionsOnDuplicateDownloadUi
    val onRequestClose = {
        component.showSolutionsOnDuplicateDownloadUi = false
    }
    state.OnFullyDismissed(onRequestClose)
    LaunchedEffect(isOpen) {
        if (isOpen) {
            state.show()
        } else {
            state.hide()
        }
    }
    val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
    ResponsiveDialog(
        onDismiss = state::hide,
        state = state,
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitleWithDescription(
                            myStringResource(Res.string.select_a_solution),
                            myStringResource(Res.string.select_download_strategy_description),
                        )
                    }
                )
            },
            content = {
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
                            onRequestClose()
                        }
                        OnDuplicateStrategySolutionItem(
                            isSelected = onDuplicateStrategy == OnDuplicateStrategy.OverrideDownload,
                            title = myStringResource(Res.string.download_strategy_override_existing_file),
                            description = myStringResource(Res.string.download_strategy_override_existing_file_description),
                        ) {
                            component.setOnDuplicateStrategy(OnDuplicateStrategy.OverrideDownload)
                            onRequestClose()
                        }
                        OnDuplicateStrategySolutionItem(
                            isSelected = null,
                            title = myStringResource(Res.string.download_strategy_update_download_link),
                            description = myStringResource(Res.string.download_strategy_update_download_link_description),
                        ) {
                            component.updateDownloadCredentialsOfOriginalDownload()
                            onRequestClose()
                        }
                        OnDuplicateStrategySolutionItem(
                            isSelected = null,
                            title = myStringResource(Res.string.download_strategy_show_downloaded_file),
                            description = myStringResource(Res.string.download_strategy_show_downloaded_file_description),
                        ) {
                            component.openDownloadFileForCurrentLink()
                            onRequestClose()
                        }
                    }
                }
            }
        )

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
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(myColors.onBackground / 10),
    )
}


@Composable
fun RenderResumeSupport(
    component: AndroidAddSingleDownloadComponent,
    modifier: Modifier,
) {
    val fileInfo by component.linkResponseInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(16.dp)
            .padding(horizontal = 8.dp)
    ) {
        val lineModifier = Modifier
            .weight(1f)
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
                        .padding(horizontal = 8.dp)
                        .size(16.dp)
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
fun ConfigActionsButtons(component: AndroidAddSingleDownloadComponent) {
    val responseInfo by component.linkResponseInfo.collectAsState()
    Row {
        IconActionButton(MyIcons.refresh, myStringResource(Res.string.refresh)) {
            component.refresh()
        }
        Spacer(Modifier.width(6.dp))
        val showMoreInputs by component.showMoreInputs.collectAsState()
        IconActionButton(
            if (showMoreInputs) {
                MyIcons.up
            } else {
                MyIcons.down
            },
            myStringResource(Res.string.more_options),
        ) {
            component.setShowMoreInputs(!showMoreInputs)
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
private fun MainActionButtons(component: AndroidAddSingleDownloadComponent) {

    val onDuplicateStrategy by component.onDuplicateStrategy.collectAsState()
    val canAddResult by component.canAddResult.collectAsState()
    if (canAddResult is CanAddResult.DownloadAlreadyExists && onDuplicateStrategy == null) {
        Row {
            val buttonModifier = Modifier.weight(1f)
            MainConfigActionButton(
                text = myStringResource(Res.string.show_solutions),
                modifier = buttonModifier,
                onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
            )
            if (component.shouldShowOpenFile.collectAsState().value) {
                Spacer(Modifier.width(8.dp))
                MainConfigActionButton(
                    text = myStringResource(Res.string.open_file),
                    modifier = buttonModifier,
                    onClick = { component.openExistingFile() },
                )
            }
        }
    } else {
        val canAddToDownloads by component.canAddToDownloads.collectAsState()
        Column {
            if (onDuplicateStrategy != null) {
                MainConfigActionButton(
                    text = myStringResource(Res.string.change_solution),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { component.showSolutionsOnDuplicateDownloadUi = true },
                )
                Space()
            }
            Row {
                val buttonModifier = Modifier.weight(1f)
                MainConfigActionButton(
                    text = myStringResource(Res.string.add),
                    modifier = buttonModifier,
                    enabled = canAddToDownloads,
                    onClick = {
                        component.shouldShowAddToQueue = true
                    },
                )
                Spacer(Modifier.width(8.dp))
                PrimaryMainActionButton(
                    text = myStringResource(Res.string.download),
                    modifier = buttonModifier,
                    enabled = canAddToDownloads,
                    onClick = {
                        component.onRequestDownload()
                    },
                )
            }

        }
    }
}

@Composable
fun RenderFileTypeAndSize(
    component: AndroidAddSingleDownloadComponent,
) {
    val isLinkLoading by component.isLinkLoading.collectAsState()
    val fileInfo by component.linkResponseInfo.collectAsState()
    val fileIconProvider = component.iconProvider
    val iconModifier = Modifier.size(mySpacings.iconSize)
    Box(
        contentAlignment = Alignment.Center,
    ) {
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

