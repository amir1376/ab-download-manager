package com.abdownloadmanager.shared.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun PauseForDurationDialog(
    request: PauseForDurationDialogRequest,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var minutesText by remember(request) { mutableStateOf("10") }
    val minutes = minutesText.toLongOrNull()
        ?.takeIf { it in 1L..MAX_CUSTOM_PAUSE_MINUTES }

    Dialog(onDismissRequest = onDismiss) {
        val shape = myShapes.defaultRounded
        Column(
            Modifier
                .clip(shape)
                .border(2.dp, myColors.onBackground / 10, shape)
                .background(
                    Brush.linearGradient(
                        listOf(myColors.surface, myColors.background)
                    )
                )
                .padding(16.dp)
                .widthIn(min = 280.dp, max = 360.dp)
        ) {
            Text(
                text = myStringResource(Res.string.pause_custom_duration_title),
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = myStringResource(Res.string.pause_custom_duration_label),
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            MyTextField(
                text = minutesText,
                onTextChange = { input ->
                    minutesText = input.filter { it in '0'..'9' }.take(7)
                },
                placeholder = "10",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = myStringResource(Res.string.pause_custom_duration_range),
                color = myColors.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButton(
                    text = myStringResource(Res.string.cancel),
                    onClick = onDismiss,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = myStringResource(Res.string.ok),
                    enabled = minutes != null,
                    onClick = { minutes?.let(onConfirm) },
                )
            }
        }
    }
}
