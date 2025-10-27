package com.abdownloadmanager.android.pages.newqueue

import androidx.compose.runtime.Composable
import com.abdownloadmanager.android.ui.configurable.SheetInput
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.MyTextField
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun NewQueueSheet(
    onQueueCreate: (String) -> Unit,
    isOpened: Boolean,
    onCloseRequest: () -> Unit,
) {
    SheetInput(
        title = Res.string.add_new_queue.asStringSource(),
        validate = { it.isNotEmpty() },
        isOpened = isOpened,
        initialValue = { "" },
        onDismiss = onCloseRequest,
        onConfirm = onQueueCreate,
        inputContent = {
            MyTextField(
                modifier = it.modifier,
                text = it.editingValue,
                onTextChange = it.setEditingValue,
                placeholder = myStringResource(Res.string.queue_name),
            )
        },
    )
}
