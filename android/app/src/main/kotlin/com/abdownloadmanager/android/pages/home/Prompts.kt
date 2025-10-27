package com.abdownloadmanager.android.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.CategoryDeletePromptState
import com.abdownloadmanager.shared.pages.home.ConfirmPromptState
import com.abdownloadmanager.shared.pages.home.DeletePromptState
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun RenderPrompts(
    component: HomeComponent,
    showConfirmPrompt: ConfirmPromptState?,
    closeConfirmPrompt: () -> Unit,
    showDeleteCategoryPrompt: CategoryDeletePromptState?,
    closeDeleteCategoryPrompt: () -> Unit,
    showDeletePromptState: DeletePromptState?,
    closeDeletePrompt: () -> Unit,
) {
    showDeletePromptState?.let {
        ShowDeletePrompts(
            deletePromptState = it,
            onCancel = {
                closeDeletePrompt()
            },
            onConfirm = {
                closeDeletePrompt()
                component.confirmDelete(it)
            })
    }
    showDeleteCategoryPrompt?.let {
        ShowDeleteCategoryPrompt(
            deletePromptState = it,
            onCancel = {
                closeDeleteCategoryPrompt()
            },
            onConfirm = {
                closeDeleteCategoryPrompt()
                component.onConfirmDeleteCategory(it)
            })
    }
    showConfirmPrompt?.let {
        ShowConfirmPrompt(
            promptState = it,
            onCancel = {
                closeConfirmPrompt()
            },
            onConfirm = {
                closeConfirmPrompt()
                showConfirmPrompt.onConfirm.invoke()
            }
        )
    }
}

@Composable
private fun ShowDeletePrompts(
    deletePromptState: DeletePromptState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(Unit) {
        state.show()
    }
    state.OnFullyDismissed(onCancel)
    // shadow the actual parameter
    ResponsiveDialog(state, state::hide) {
        deletePromptState?.let { deletePromptState ->
            SheetUI(
                header = {
                    SheetHeader(
                        headerTitle = {
                            SheetTitle(myStringResource(Res.string.confirm_delete_download_items_title))
                        }
                    )
                }
            ) {
                Column(
                    Modifier
                        .padding(horizontal = mySpacings.largeSpace)
                        .padding(bottom = mySpacings.largeSpace)
                ) {
                    val finishedCount = deletePromptState.finishedCount
                    val unfinishedCount = deletePromptState.unfinishedCount
                    Text(
                        when {
                            deletePromptState.hasBothFinishedAndUnfinished() -> {
                                Res.string.confirm_delete_download_finished_and_unfinished_items_description.asStringSourceWithARgs(
                                    Res.string.confirm_delete_download_finished_and_unfinished_items_description_createArgs(
                                        finishedCount = finishedCount.toString(),
                                        unfinishedCount = unfinishedCount.toString(),
                                    )
                                )
                            }

                            deletePromptState.hasUnfinishedDownloads -> {
                                Res.string.confirm_delete_download_unfinished_items_description.asStringSourceWithARgs(
                                    Res.string.confirm_delete_download_unfinished_items_description_createArgs(
                                        count = unfinishedCount.toString(),
                                    )
                                )
                            }

                            else -> {
                                Res.string.confirm_delete_download_items_description.asStringSourceWithARgs(
                                    Res.string.confirm_delete_download_items_description_createArgs(
                                        count = finishedCount.toString()
                                    ),
                                )
                            }
                        }.rememberString(),
                        fontSize = myTextSizes.base,
                        color = myColors.onBackground,
                    )
                    if (deletePromptState.hasFinishedDownloads) {
                        Spacer(Modifier.height(12.dp))
                        val alsoDeleteFileInteractionSource = remember { MutableInteractionSource() }
                        Row(
                            Modifier
                                .clickable(
                                    interactionSource = alsoDeleteFileInteractionSource,
                                    indication = null
                                ) {
                                    deletePromptState.alsoDeleteFile = !deletePromptState.alsoDeleteFile
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CheckBox(
                                value = deletePromptState.alsoDeleteFile,
                                onValueChange = {
                                    deletePromptState.alsoDeleteFile = it
                                },
                                modifier = Modifier
                                    // the Row itself is clickable (focusable) so we don't need to focus this checkbox
                                    // is there a better way?
                                    .focusProperties { canFocus = false },
                                interactionSource = alsoDeleteFileInteractionSource,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                myStringResource(Res.string.also_delete_file_from_disk),
                                fontSize = myTextSizes.base,
                                color = myColors.onBackground,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActionButton(
                            text = myStringResource(Res.string.cancel),
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        ActionButton(
                            text = myStringResource(Res.string.delete),
                            onClick = onConfirm,
                            borderColor = SolidColor(myColors.error),
                            contentColor = myColors.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowConfirmPrompt(
    promptState: ConfirmPromptState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(Unit) {
        state.show()
    }
    state.OnFullyDismissed(onCancel)
    ResponsiveDialog(
        state, state::hide,
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(promptState.title.rememberString())
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(horizontal = mySpacings.largeSpace)
                    .padding(bottom = mySpacings.largeSpace)
            ) {
                Text(
                    text = promptState.description.rememberString(),
                    fontSize = myTextSizes.base,
                    color = myColors.onBackground,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        text = myStringResource(Res.string.cancel),
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    ActionButton(
                        text = myStringResource(Res.string.ok),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowDeleteCategoryPrompt(
    deletePromptState: CategoryDeletePromptState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(Unit) {
        state.show()
    }
    state.OnFullyDismissed(onCancel)
    ResponsiveDialog(state, state::hide) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            myStringResource(
                                Res.string.confirm_delete_category_item_title,
                                Res.string.confirm_delete_category_item_title_createArgs(
                                    name = deletePromptState.category.name
                                ),
                            )
                        )
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(horizontal = mySpacings.largeSpace)
                    .padding(bottom = mySpacings.largeSpace)
            ) {
                Text(
                    myStringResource(
                        Res.string.confirm_delete_category_item_description,
                        Res.string.confirm_delete_category_item_description_createArgs(
                            value = deletePromptState.category.name
                        )
                    ),
                    fontSize = myTextSizes.base,
                    color = myColors.onBackground,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    myStringResource(Res.string.your_download_will_not_be_deleted),
                    fontSize = myTextSizes.base,
                    color = myColors.onBackground,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        text = myStringResource(Res.string.cancel),
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(mySpacings.mediumSpace))
                    ActionButton(
                        text = myStringResource(Res.string.delete),
                        onClick = onConfirm,
                        borderColor = SolidColor(myColors.error),
                        modifier = Modifier.weight(1f),
                        contentColor = myColors.error,
                    )
                }
            }
        }
    }
}
