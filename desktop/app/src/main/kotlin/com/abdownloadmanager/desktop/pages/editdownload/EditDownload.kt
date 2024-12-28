package com.abdownloadmanager.desktop.pages.editdownload

import androidx.compose.runtime.Composable

import com.abdownloadmanager.utils.compose.WithContentAlpha
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.*
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.abdownloadmanager.desktop.pages.addDownload.shared.ExtraConfig
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.theme.LocalUiScale
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.FileIconProvider
import com.abdownloadmanager.utils.compose.WithContentColor
import ir.amirab.util.UrlUtils
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun EditDownloadWindow(
    component: EditDownloadComponent,
) {
    CustomWindow(
        state = rememberWindowState(
            size = DpSize(450.dp, 230.dp)
                .applyUiScale(LocalUiScale.current),
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        alwaysOnTop = true,
        onCloseRequest = {
            component.onRequestClose()
        },
    ) {
        HandleEffects(component) {
            when (it) {
                EditDownloadPageEffects.BringToFront -> {
                    window.toFront()
                }
            }
        }
        EditDownloadPage(component)
    }
}

@Composable
fun EditDownloadPage(
    component: EditDownloadComponent,
) {
    WindowTitle(myStringResource(Res.string.edit_download_title))
    component.editDownloadUiChecker.collectAsState().value?.let { editDownloadUiChecker ->
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            val canAddResult by editDownloadUiChecker.canEditDownloadResult.collectAsState()
            val link by editDownloadUiChecker.link.collectAsState()
            fun setLink(link: String) {
                editDownloadUiChecker.setLink(link)
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
            Row {
                Column(Modifier.weight(1f)) {
                    val name by editDownloadUiChecker.name.collectAsState()
                    Spacer(Modifier.size(8.dp))
                    NameTextField(
                        text = name,
                        setText = {
                            editDownloadUiChecker.setName(it)
                        },
                        errorText = when (canAddResult) {
                            CanEditDownloadResult.FileNameAlreadyExists -> Res.string.file_name_already_exists
                            CanEditDownloadResult.InvalidFileName -> Res.string.invalid_file_name
                            else -> null
                        }?.takeIf { name.isNotEmpty() }?.asStringSource()?.rememberString()
                    )
                    Spacer(Modifier.size(8.dp))
                    BrowserImportButton(component, editDownloadUiChecker)
                }
                Spacer(Modifier.size(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .width(IntrinsicSize.Max)
                ) {
                    RenderFileTypeAndSize(component.iconProvider, editDownloadUiChecker)
                    RenderResumeSupport(editDownloadUiChecker)
                    ConfigActionsButtons(editDownloadUiChecker)
                }
            }
            Spacer(Modifier.weight(1f))
            MainActionButtons(component, editDownloadUiChecker)
            if (editDownloadUiChecker.showMoreSettings.collectAsState().value) {
                ExtraConfig(
                    onDismiss = {
                        editDownloadUiChecker.setShowMoreSettings(false)
                    },
                    configurables = editDownloadUiChecker.configurables,
                )
            }
        }
    }
}

@Composable
fun BrowserImportButton(
    component: EditDownloadComponent,
    downloadUiState: EditDownloadState,
) {
    val credentialsImportedFromExternal by component.credentialsImportedFromExternal.collectAsState()
    val downloadPage = downloadUiState.currentDownloadItem.collectAsState().value.downloadPage
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionButton(
                myStringResource(Res.string.edit_download_update_from_download_page),
                enabled = downloadPage != null,
                onClick = {
                    downloadPage?.let {
                        UrlUtils.openUrl(it)
                    }
                },
//                borderColor = when (credentialsImportedFromExternal) {
//                    true -> SolidColor(myColors.success)
//                    false -> SolidColor(myColors.onBackground / 10)
//                },
                contentPadding = PaddingValues(
                    vertical = 6.dp,
                    horizontal = animateDpAsState(
                        if (credentialsImportedFromExternal) 8.dp
                        else 16.dp
                    ).value,
                ),
                end = {
                    AnimatedVisibility(credentialsImportedFromExternal) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            MyIcon(
                                MyIcons.check,
                                null,
                                Modifier.size(16.dp),
                                tint = myColors.success,
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            Help(myStringResource(Res.string.edit_download_update_from_download_page_description))
        }
    }
}

@Composable
private fun RenderResumeSupport(
    editDownloadUiChecker: EditDownloadState,
) {
    val fileInfo by editDownloadUiChecker.responseInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(16.dp)
    ) {
        val lineModifier = Modifier.weight(1f)
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
fun ConfigActionsButtons(
    editDownloadUiChecker: EditDownloadState,
) {
    val showMoreSettings by editDownloadUiChecker.showMoreSettings.collectAsState()
    val requiresAuth = editDownloadUiChecker.responseInfo.collectAsState().value?.requireBasicAuth ?: false
    Row {
        IconActionButton(MyIcons.refresh, myStringResource(Res.string.refresh)) {
            editDownloadUiChecker.refresh()
        }
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            MyIcons.settings,
            myStringResource(Res.string.settings),
            indicateActive = showMoreSettings,
            requiresAttention = requiresAuth
        ) {
            editDownloadUiChecker.setShowMoreSettings(true)
        }
    }
}

@Composable
private fun MainActionButtons(
    component: EditDownloadComponent,
    editDownloadUiChecker: EditDownloadState,
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
        Box {
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
            PrimaryMainConfigActionButton(
                text = myStringResource(Res.string.change),
                modifier = Modifier,
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
fun WarningPrompt(
    warnings: List<CanEditWarnings>,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    Popup(
        popupPositionProvider = rememberComponentRectPositionProvider(
            anchor = Alignment.TopStart,
            alignment = Alignment.TopEnd,
        ),
        onDismissRequest = onClose
    ) {
        val shape = RoundedCornerShape(6.dp)
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
    editDownloadUiChecker: EditDownloadState,
) {
    val isLinkLoading by editDownloadUiChecker.isLinkLoading.collectAsState()
    val fileInfo by editDownloadUiChecker.responseInfo.collectAsState()
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
    MyIcon(icon, null, Modifier
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
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    AddDownloadPageTextField(
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
    AddDownloadPageTextField(
        text,
        setText,
        myStringResource(Res.string.name),
        modifier = Modifier.fillMaxWidth(),
        errorText = errorText,
    )
}

@Composable
private fun AddDownloadPageTextField(
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

