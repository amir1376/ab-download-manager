package com.abdownloadmanager.android.pages.home

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.asStringSource

@Immutable
data class QueueSelectedItemsMenuProps(
    val queueName: String,
    val onRequestQueueItemsUp: () -> Unit,
    val onRequestQueueItemsDown: () -> Unit,
    val onRequestRemoveItemsFromQueue: () -> Unit,
)

@Composable
fun RenderSelectedQueueItemsOption(
    props: QueueSelectedItemsMenuProps,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransparentIconActionButton(
            icon = MyIcons.up,
            contentDescription = Res.string.move_up.asStringSource(),
            onClick = props.onRequestQueueItemsUp,
            shape = RectangleShape,
        )
        TransparentIconActionButton(
            icon = MyIcons.down,
            contentDescription = Res.string.move_down.asStringSource(),
            onClick = props.onRequestQueueItemsDown,
            shape = RectangleShape,
        )
        Text(
            props.queueName,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        TransparentIconActionButton(
            MyIcons.minus,
            Res.string.remove.asStringSource(),
            onClick = props.onRequestRemoveItemsFromQueue,
            shape = RectangleShape,
        )
    }
}
