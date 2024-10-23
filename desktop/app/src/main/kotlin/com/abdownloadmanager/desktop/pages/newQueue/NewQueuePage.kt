package com.abdownloadmanager.desktop.pages.newQueue

import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.MyTextField
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.resources.*
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun NewQueue(
    onQueueCreate: (String) -> Unit,
    onCloseRequest: () -> Unit,
) {
    WindowTitle(myStringResource(Res.string.add_new_queue))
    var name by remember {
        mutableStateOf("")
    }
    val focusRequester= remember { FocusRequester() }
    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
    Column(Modifier) {
        Spacer(Modifier.height(8.dp))
        MyTextField(
            text = name,
            onTextChange = {
                name = it
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(horizontal = 8.dp)
                .widthIn(max = 400.dp),
            placeholder = myStringResource(Res.string.queue_name),
        )
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            ActionButton(
                text = myStringResource(Res.string.add),
                onClick = {
                    onQueueCreate(name)
                }
            )
            Spacer(Modifier.width(4.dp))
            ActionButton(
                text = myStringResource(Res.string.cancel),
                onClick = {
                    onCloseRequest()
                }
            )
        }
    }
}
