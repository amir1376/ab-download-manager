package com.abdownloadmanager.desktop.ui.widget.menu

import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.utils.action.MenuItem
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider

@Composable
fun ShowOptionsInDropDown(
    menu: MenuItem.SubMenu,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = rememberCursorPositionProvider(
            alignment = Alignment.BottomEnd
        ),
        onDismissRequest = onDismissRequest
    ) {
        ShowOptions(menu,onDismissRequest)
    }
}

@Composable
fun ShowOptions(
    menu: MenuItem.SubMenu,
    onDismissRequest: () -> Unit
) {
    SubMenu(menu,onDismissRequest) {
        Column(
            Modifier
                .width(200.dp)
        ) {
            val itemPadding = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            val title by menu.title.collectAsState()
            Text(
                title,
                Modifier
                    .then(itemPadding)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        delayMillis = 0
                    ),
                fontSize = myTextSizes.base,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Spacer(Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onSurface/5))
        }
    }
}