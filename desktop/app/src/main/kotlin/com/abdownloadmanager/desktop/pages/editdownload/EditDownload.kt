package com.abdownloadmanager.desktop.pages.editdownload

import androidx.compose.runtime.Composable

import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
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
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.desktop.pages.addDownload.shared.ExtraConfig
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.edit.TAEditDownloadInputs
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun EditDownloadWindow(
    component: DesktopEditDownloadComponent,
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
    component: DesktopEditDownloadComponent,
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
                    configurables = editDownloadUiChecker.configurableList,
                )
            }
        }
    }
}

@Composable
fun BrowserImportButton(
    component: DesktopEditDownloadComponent,
    downloadUiState: EditDownloadInputs<*, *, *, *, *, *>,
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
                        URLOpener.openUrl(it)
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
    editDownloadUiChecker: TAEditDownloadInputs,
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
fun ConfigActionsButtons(
    editDownloadUiChecker: TAEditDownloadInputs,
) {
    val showMoreSettings by editDownloadUiChecker.showMoreSettings.collectAsState()
    val requiresAuth = editDownloadUiChecker.responseInfo.collectAsState().value?.requireBasicAuth ?: false
    Row {
        IconActionButton(MyIcons.refresh, Res.string.refresh.asStringSource()) {
            editDownloadUiChecker.refresh()
        }
        Spacer(Modifier.width(6.dp))
        IconActionButton(
            MyIcons.settings,
            Res.string.settings.asStringSource(),
            indicateActive = showMoreSettings,
            requiresAttention = requiresAuth
        ) {
            editDownloadUiChecker.setShowMoreSettings(true)
        }
    }
}

@Composable
private fun MainActionButtons(
    component: DesktopEditDownloadComponent,
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
            PrimaryMainActionButton(
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

