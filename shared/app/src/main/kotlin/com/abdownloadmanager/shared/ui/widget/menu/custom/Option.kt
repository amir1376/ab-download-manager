package com.abdownloadmanager.shared.ui.widget.menu.custom

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.compose.action.MenuItem
import com.abdownloadmanager.shared.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                title.rememberString(),
                Modifier
                    .then(itemPadding)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        initialDelayMillis = 0
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