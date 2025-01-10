package com.abdownloadmanager.shared.ui.widget

import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.amirab.util.compose.StringSource


@Composable
fun MyTabRow(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        content()
    }
}

@Composable
fun MyTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: IconSource,
    title: StringSource,
    selectionBackground: Color = myColors.background,
) {
    WithContentAlpha(
        if (selected) 1f else 0.75f
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.let {
                if (selected) {
                    it
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(selectionBackground)
                } else {
                    it
                }
            }
                .onClick { onClick() }
                .padding(horizontal = 12.dp)
                .padding(vertical = 6.dp)

        ) {
            MyIcon(icon, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(title.rememberString(), maxLines = 1, fontSize = myTextSizes.base)
        }
    }
}
