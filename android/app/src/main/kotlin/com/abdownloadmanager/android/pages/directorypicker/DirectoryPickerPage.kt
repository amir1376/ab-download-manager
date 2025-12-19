package com.abdownloadmanager.android.pages.directorypicker

import android.os.Environment
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.rememberAppPermissionState
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitleWithDescription
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.android.ui.configurable.SheetInput
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.PathValidator
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.createDirectories
import ir.amirab.util.exists
import ir.amirab.util.listFiles
import ir.amirab.util.listFilesOrNull
import ir.amirab.util.startsWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toOkioPath
import kotlin.system.exitProcess

val alwaysAllowedPaths = listOf(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toOkioPath(),
)

@Composable
fun DirectoryPicker(
    title: StringSource,
    isVisible: Boolean,
    initialDirectory: Path,
    onDirectorySelected: (Path?) -> Unit
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(isVisible) {
        if (isVisible) {
            state.show()
        } else {
            state.hide()
        }
    }
    state.OnFullyDismissed {
        onDirectorySelected(null)
    }
    val onDismiss = state::hide
    ResponsiveDialog(
        state,
        onDismiss
    ) {
        var currentDirectory by remember(initialDirectory) {
            mutableStateOf(initialDirectory)
        }
        var creatingNewFolder by remember { mutableStateOf(false) }
        // update this counter in order to refresh directory list!
        var updateDirectories by remember { mutableIntStateOf(0) }
        fun refreshDirectories() {
            updateDirectories++
        }

        val storagePermissionState = rememberAppPermissionState(ABDMPermissions.StoragePermission)
        val directoryList = remember(
            currentDirectory,
            updateDirectories,
            storagePermissionState.isGranted,
        ) {
            val weHaveFullAccess = storagePermissionState.isGranted
            DirectoryList(
                currentDirectory = currentDirectory,
                directories = runCatching { currentDirectory.listFiles() }
                    .getOrNull()
                    .orEmpty()
                    .map {
                        DirectoryItem(it.name, it)
                    },
                backDirectory = currentDirectory
                    .parent
                    // don't go somewhere that we can't return
                    ?.takeIf {
                        it.listFilesOrNull()?.isNotEmpty() ?: false
                    },
                currentDirectoryCanWrite = if (weHaveFullAccess) {
                    true
                } else {
                    alwaysAllowedPaths.any { allowedPath ->
                        currentDirectory.startsWith(allowedPath)
                    }
                }
            )
        }
        val coroutineScope = rememberCoroutineScope()
        fun createNewFolderAndRefresh(newFolderName: String) {
            creatingNewFolder = false
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    currentDirectory.resolve(newFolderName).createDirectories()
                }
                delay(50)
                refreshDirectories()
                // schedule refresh
            }
        }
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitleWithDescription(
                            title = title.rememberString(),
                            description = currentDirectory.toString()
                        )
                    },
                    headerActions = {
                        TransparentIconActionButton(
                            MyIcons.close,
                            contentDescription = myStringResource(Res.string.close),
                            onClick = onDismiss
                        )
                    }
                )
            }
        ) {
            val horizontalPadding = mySpacings.largeSpace
            val itemPadding = PaddingValues(
                horizontal = mySpacings.largeSpace,
                vertical = mySpacings.mediumSpace
            )
            Column {
                val lazyListState = rememberLazyListState()
                AnimatedContent(
                    directoryList,
                    modifier = Modifier
                        .weight(1f, false)
                ) { directoryList ->
                    VerticalScrollableContent(
                        lazyListState = lazyListState,
                    ) {
                        Box(
                            Modifier.heightIn(250.dp)
                        ) {
                            LazyColumn {
                                if (directoryList.backDirectory != null) {
                                    item {
                                        val backDirectoryItem = remember(directoryList.currentDirectory) {
                                            DirectoryItem(
                                                name = "..",
                                                path = directoryList.backDirectory
                                            )
                                        }
                                        RenderDirectoryItem(
                                            modifier = Modifier
                                                .animateItem()
                                                .fillMaxWidth(),
                                            item = backDirectoryItem,
                                            onDirectorySelected = {
                                                currentDirectory = backDirectoryItem.path
                                            },
                                            itemPadding = itemPadding,
                                        )
                                    }
                                }
                                items(directoryList.directories) { directoryItem ->
                                    RenderDirectoryItem(
                                        modifier = Modifier
                                            .animateItem()
                                            .fillMaxWidth(),
                                        item = directoryItem,
                                        onDirectorySelected = {
                                            currentDirectory = directoryItem.path
                                        },
                                        itemPadding = itemPadding,
                                    )
                                }
                            }
                            if (directoryList.directories.isEmpty()) {
                                Text(
                                    myStringResource(Res.string.list_is_empty),
                                    Modifier
                                        .matchParentSize()
                                        .wrapContentSize()
                                )
                            }
                        }
                    }

                }
                Spacer(Modifier.height(mySpacings.mediumSpace))
                Column(
                    Modifier.padding(horizontal = horizontalPadding)
                ) {
                    AnimatedVisibility(!directoryList.currentDirectoryCanWrite && !storagePermissionState.isGranted) {
                        ActionButton(
                            text = myStringResource(Res.string.give_storage_permission),
                            onClick = {
                                storagePermissionState.launchRequest()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = mySpacings.mediumSpace),
                            borderColor = myColors.warningGradient,
                            contentColor = myColors.warning,
                            start = {
                                MyIcon(
                                    icon = storagePermissionState.appPermission.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = mySpacings.mediumSpace)
                                )
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActionButton(
                            text = myStringResource(Res.string.ok),
                            onClick = {
                                onDirectorySelected(currentDirectory)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = directoryList.currentDirectoryCanWrite,
                        )
                        Spacer(Modifier.width(mySpacings.mediumSpace))
                        IconActionButton(
                            MyIcons.add,
                            contentDescription = myStringResource(Res.string.new_folder),
                            enabled = directoryList.currentDirectoryCanWrite,
                        ) {
                            creatingNewFolder = true
                        }
                    }
                }
            }
            SheetInput(
                isOpened = creatingNewFolder,
                title = Res.string.new_folder.asStringSource(),
                initialValue = { "" },
                validate = {
                    val newFolder = runCatching { currentDirectory.resolve(it) }.getOrNull() ?: return@SheetInput false
                    PathValidator.isValidPath(newFolder.toString()) && !newFolder.exists()
                },
                onConfirm = { newFolderName ->
                    createNewFolderAndRefresh(newFolderName)
                },
                onDismiss = {
                    creatingNewFolder = false
                }
            ) { params ->
                MyTextField(
                    text = params.editingValue,
                    onTextChange = params.setEditingValue,
                    modifier = params.modifier,
                    placeholder = "New Folder",
                    keyboardActions = params.keyboardActions,
                )
            }
        }
    }
}

@Composable
private fun RenderDirectoryItem(
    modifier: Modifier,
    item: DirectoryItem,
    onDirectorySelected: () -> Unit,
    itemPadding: PaddingValues,
) {
    Row(
        modifier
            .clickable(
                onClick = onDirectorySelected
            )
            .heightIn(mySpacings.thumbSize)
            .padding(itemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyIcon(MyIcons.folder, null)
        Spacer(Modifier.width(mySpacings.mediumSpace))
        Text(item.name)
    }
}

@Immutable
private data class DirectoryList(
    val currentDirectory: Path,
    val backDirectory: Path?,
    val directories: List<DirectoryItem>,
    val currentDirectoryCanWrite: Boolean,
)

@Immutable
private data class DirectoryItem(
    val name: String,
    val path: Path,
)
