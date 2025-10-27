package com.abdownloadmanager.shared.ui.widget.menu.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.myColors

@Composable
fun MenuColumn(
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = LocalMenuBoxClip.current
    Column(
        Modifier.Companion
            .shadow(24.dp)
//                .verticalScroll(rememberScrollState())
            .clip(shape)
            .width(IntrinsicSize.Max)
            .widthIn(120.dp)
            .border(1.dp, myColors.surface, shape)
            .background(myColors.menuGradientBackground)
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        content()
    }
}
