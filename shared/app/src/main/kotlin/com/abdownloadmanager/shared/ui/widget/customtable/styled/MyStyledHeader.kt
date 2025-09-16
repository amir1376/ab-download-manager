package com.abdownloadmanager.shared.ui.widget.customtable.styled

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.ui.widget.customtable.TableScope
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.theme.myShapes

@Composable
fun TableScope.MyStyledTableHeader(
    itemHorizontalPadding: Dp,
    content:@Composable ()->Unit,
) {
    val shape = myShapes.defaultRounded
    WithContentAlpha(0.75f) {
        Box(Modifier
            .widthIn(getTableSize().visibleWidth)
            .padding(bottom = 1.dp)
            .shadow(
                elevation = 1.dp,
                shape = shape,
            )
            .padding(bottom = 1.dp)
            .clip(shape)
            .background(myColors.surface)
            .padding(vertical = 8.dp, horizontal = itemHorizontalPadding)
        ) {
            content()
        }
    }
}
