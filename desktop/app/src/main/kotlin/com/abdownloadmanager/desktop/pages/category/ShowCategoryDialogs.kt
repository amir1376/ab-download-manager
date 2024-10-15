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
        CustomWindow(
            onCloseRequest = {
                d.close()
            },
            alwaysOnTop = true,
            state = rememberWindowState(
                size = DpSize(350.dp, 350.dp)
            )
        ) {
            CategoryDialog(d)
        }
    }
}

@Composable
private fun CategoryDialog(
    component: CategoryComponent,
) {
    NewCategory(component)
}