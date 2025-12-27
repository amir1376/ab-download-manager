package com.abdownloadmanager.android.pages.home

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.abdownloadmanager.android.ui.menu.RenderMenuInSinglePage
import com.abdownloadmanager.shared.ui.widget.rememberMyComponentRectPositionProvider

@Composable
fun RenderAddMenu(component: HomeComponent) {
    val mainMenuShowing by component.isAddMenuShowing.collectAsState()
    if (mainMenuShowing) {
        val onDismissRequest = {
            component.setIsAddMenuShowing(false)
        }
        Popup(
            popupPositionProvider = rememberMyComponentRectPositionProvider(
                anchor = Alignment.TopEnd,
                alignment = Alignment.TopStart,
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            ),
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
            ),
        ) {
            RenderMenuInSinglePage(
                menu = component.addMenu,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.width(IntrinsicSize.Max),
            )
        }
    }
}
