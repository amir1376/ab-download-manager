package com.abdownloadmanager.desktop.pages.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow

@Composable
fun ShowCategoryDialogs(dialogManager: CategoryDialogManager) {
    val dialogs by dialogManager.openedCategoryDialogs.collectAsState()
    for (d in dialogs) {
        CategoryDialog(d)
    }
}

@Composable
private fun CategoryDialog(
    component: CategoryComponent,
) {
    CustomWindow(
        onCloseRequest = {
            component.close()
        },
        alwaysOnTop = true,
        state = rememberWindowState(
            size = DpSize(350.dp, 400.dp)
        )
    ) {
        NewCategory(component)
    }
}