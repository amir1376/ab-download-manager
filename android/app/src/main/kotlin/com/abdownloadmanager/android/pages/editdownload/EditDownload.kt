package com.abdownloadmanager.android.pages.editdownload

import androidx.compose.runtime.Composable

import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.abdownloadmanager.android.pages.add.shared.ExtraConfig
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.shared.ui.widget.*
import ir.amirab.util.ifThen
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.TAEditDownloadInputs
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.asStringSource

@Composable
fun EditDownloadSheet(
    component: AndroidEditDownloadComponent?,
    onDismiss: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    state.OnFullyDismissed(onDismiss)
    LaunchedEffect(component) {
        if (component == null) {
            state.hide()
        } else {
            state.show()
        }
    }
    ResponsiveDialog(state, state::hide) {
        component?.let {
            EditDownloadPage(component, state::hide)
        }
    }
}

@Composable
fun ResponsiveDialogScope.EditDownloadPage(
    component: AndroidEditDownloadComponent,
    onDismiss: () -> Unit,
) {
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    SheetTitle(myStringResource(Res.string.edit_download_title))
                },
                headerActions = {
                    TransparentIconActionButton(
                        MyIcons.close,
                        contentDescription = Res.string.close.asStringSource(),
                        onClick = onDismiss
                    )
                },
            )
        }
    ) {
        component.editDownloadUiChecker.collectAsState().value?.let { downloadInputs ->
            Column(
                Modifier
                    .padding(mySpacings.mediumSpace)
            ) {
                val canAddResult by downloadInputs.canEditDownloadResult.collectAsState()
                val link by downloadInputs.link.collectAsState()
                fun setLink(link: String) {
                    downloadInputs.setLink(link)
                }

                val linkFocus = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    linkFocus.requestFocus()
                }

                UrlTextField(
                    text = link,
                    setText = {
                        setLink(it)
                    },
                    modifier = Modifier.focusRequester(linkFocus),
                    errorText = when (canAddResult) {
                        CanEditDownloadResult.InvalidURL -> Res.string.invalid_url
                        else -> null
                    }?.takeIf { link.isNotEmpty() }?.asStringSource()?.rememberString()
                    // ATTENTION DO NOT use composable functions in when branches
                    // it seems buggy (compose won't render ui properly)
                    // stranger part is that in this case if we use ? before takeIf then it will work! (`}.takeIf {` is  buggy but `}?.takeIf {` works!)
                    // maybe there is a bug in compose compiler, or maybe I'm missed something. if you read this ,and you know why! please let me know!
                )
                val name by downloadInputs.name.collectAsState()
                Spacer(Modifier.size(8.dp))
                NameTextField(
                    text = name,
                    setText = {
                        downloadInputs.setName(it)
                    },
                    errorText = when (canAddResult) {
                        CanEditDownloadResult.FileNameAlreadyExists -> Res.string.file_name_already_exists
                        CanEditDownloadResult.InvalidFileName -> Res.string.invalid_file_name
                        else -> null
                    }?.takeIf { name.isNotEmpty() }?.asStringSource()?.rememberString()
                )
                Spacer(Modifier.size(8.dp))
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RenderFileTypeAndSize(component.iconProvider, downloadInputs)
                    RenderResumeSupport(downloadInputs, Modifier.weight(1f))
                    ConfigActionsButtons(downloadInputs)
                }
                Spacer(Modifier.size(8.dp))
                MainActionButtons(component, downloadInputs)
                val showMoreSettings by downloadInputs.showMoreSettings.collectAsState()
                ExtraConfig(
                    onDismiss = {
                        downloadInputs.setShowMoreSettings(false)
                    },
                    configurables = downloadInputs.configurableList,
                    isOpened = showMoreSettings,
                )
            }
        }
    }
}

@Composable
fun BrowserImportButton(
    downloadUiState: EditDownloadInputs<*, *, *, *, *, *>,
) {
    val downloadPage = downloadUiState.currentDownloadItem.collectAsState().value.downloadPage
    IconActionButton(
        MyIcons.earth,
        Res.string.edit_download_update_from_download_page.asStringSource(),
        enabled = downloadPage != null,
        onClick = {
            downloadPage?.let {
                URLOpener.openUrl(it)
            }
        }
    )
}

@Composable
private fun RenderResumeSupport(
    editDownloadUiChecker: TAEditDownloadInputs,
    modifier: Modifier,
) {
    val fileInfo by editDownloadUiChecker.responseInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(16.dp)
    ) {
        val lineModifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(myColors.onBackground / 10)
        Box(lineModifier)
        val canEditDownload by editDownloadUiChecker.canEdit.collectAsState()
        AnimatedVisibility(
            visible = canEditDownload && fileInfo != null,
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
fun ConfigActionsButtons(
    downloadInputs: TAEditDownloadInputs,
) {
    val showMoreSettings by downloadInputs.showMoreSettings.collectAsState()
    val requiresAuth = downloadInputs.responseInfo.collectAsState().value?.requireBasicAuth ?: false
    Row {
        IconActionButton(MyIcons.refresh, Res.string.refresh.asStringSource()) {
            downloadInputs.refresh()
        }
        Spacer(Modifier.width(6.dp))
        BrowserImportButton(downloadInputs)
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            MyIcons.settings,
            Res.string.settings.asStringSource(),
            indicateActive = showMoreSettings,
            requiresAttention = requiresAuth
        ) {
            downloadInputs.setShowMoreSettings(true)
        }
    }
}

@Composable
private fun MainActionButtons(
    component: AndroidEditDownloadComponent,
    editDownloadUiChecker: TAEditDownloadInputs,
) {
    Row {
        val canEditResult by editDownloadUiChecker.canEditDownloadResult.collectAsState()

        val canEdit = run {
            val canBeEdited = editDownloadUiChecker.canEdit.collectAsState().value
            val componentAllowsEdit = component.acceptEdit.collectAsState().value
            canBeEdited && componentAllowsEdit
        }
        val warnings = (canEditResult as? CanEditDownloadResult.CanEdit)?.warnings.orEmpty()
        Spacer(Modifier.width(8.dp))
        var showWarningPrompt by remember {
            mutableStateOf(false)
        }
        MainConfigActionButton(
            text = myStringResource(Res.string.cancel),
            modifier = Modifier.weight(1f),
            onClick = {
                component.onRequestClose()
            },
        )
        Spacer(Modifier.width(mySpacings.mediumSpace))
        Box(Modifier.weight(1f)) {
            if (showWarningPrompt) {
                WarningPrompt(
                    warnings = warnings,
                    onClose = {
                        showWarningPrompt = false
                    },
                    onConfirm = {
                        if (canEdit) {
                            component.onRequestEdit()
                        }
                    }
                )
            }
            PrimaryMainActionButton(
                text = myStringResource(Res.string.change),
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit,
                onClick = {
                    if (warnings.isNotEmpty()) {
                        showWarningPrompt = true
                    } else {
                        component.onRequestEdit()
                    }
                },
            )
        }
    }
}

@Composable
fun WarningPrompt(
    warnings: List<CanEditWarnings>,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    Popup(
        popupPositionProvider = rememberMyComponentRectPositionProvider(
            anchor = Alignment.TopStart,
            alignment = Alignment.TopEnd,
        ),
        onDismissRequest = onClose
    ) {
        val shape = myShapes.defaultRounded
        Box(
            Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 240.dp)
                .shadow(24.dp)
                .clip(shape)
                .border(1.dp, myColors.surface, shape)
                .background(myColors.menuGradientBackground)
                .padding(8.dp)
        ) {
            WithContentColor(myColors.onSurface) {
                Column {
                    Text(
                        myStringResource(Res.string.warning),
                        fontWeight = FontWeight.Bold,
                        color = myColors.warning
                    )
                    Spacer(Modifier.height(4.dp))
                    warnings.forEach {
                        Text(
                            it.asStringSource().rememberString(),
                            fontSize = myTextSizes.base,
                        )
                    }
                    Text(myStringResource(Res.string.warning_you_may_have_to_restart_the_download_later))
                    Spacer(Modifier.height(8.dp))
                    ActionButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = myStringResource(Res.string.change_anyway),
                        onClick = onConfirm,
                        borderColor = SolidColor(myColors.error),
                        contentColor = myColors.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderFileTypeAndSize(
    iconProvider: FileIconProvider,
    editDownloadUiChecker: TAEditDownloadInputs,
) {
    val isLinkLoading by editDownloadUiChecker.isLinkLoading.collectAsState()
    val fileInfo by editDownloadUiChecker.responseInfo.collectAsState()
    val iconModifier = Modifier.size(mySpacings.iconSize)
    Box(Modifier) {
        AnimatedContent(
            targetState = isLinkLoading,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { loading ->
            if (loading) {
                LoadingIndicator(iconModifier)
            } else {
                val icon = iconProvider.rememberIcon(editDownloadUiChecker.name.collectAsState().value)
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

                                val size by editDownloadUiChecker.lengthStringFlow.collectAsState()
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
private fun MyTextFieldIcon(
    icon: IconSource,
    onClick: (() -> Unit)? = null,
) {
    MyIcon(
        icon, null, Modifier
            .fillMaxHeight()
            .ifThen(onClick != null) {
                pointerHoverIcon(PointerIcon.Default)
                    .clickable { onClick?.invoke() }
            }
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
            .size(16.dp))
}


@Composable
private fun UrlTextField(
    text: String,
    setText: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
) {
    MyTextFieldWithIcons(
        text,
        setText,
        myStringResource(Res.string.download_link),
        modifier = modifier.fillMaxWidth(),
        start = {
            MyTextFieldIcon(MyIcons.link)
        },
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

