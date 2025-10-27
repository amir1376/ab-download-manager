package com.abdownloadmanager.shared.ui.widget.menu.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.amirab.util.compose.action.MenuItem

@Composable
expect fun WithContextMenu(
    menuProvider: () -> List<MenuItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
