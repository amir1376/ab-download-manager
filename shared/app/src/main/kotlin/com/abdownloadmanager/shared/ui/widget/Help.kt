package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon

@Composable
fun Help(
    content: String,
) {
    var showHelpContent by remember { mutableStateOf(false) }
    val onRequestCloseShowHelpContent = {
        showHelpContent = false
    }
    Column {
        MyIcon(
            MyIcons.question,
            "Hint",
            Modifier
                .clip(CircleShape)
                .clickable {
                    showHelpContent = !showHelpContent
                }
                .border(
                    1.dp,
                    if (showHelpContent) myColors.primary
                    else Color.Transparent,
                    CircleShape
                )
                .background(myColors.surface)
                .padding(4.dp)
                .size(12.dp),
            tint = myColors.onSurface,
        )
        if (showHelpContent) {
            TooltipPopup(
                onRequestCloseShowHelpContent = onRequestCloseShowHelpContent,
                content = content,
            )
        }
    }
}