package com.abdownloadmanager.desktop.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.nucleusframework.composenativetray.menu.api.ComposableTrayMenuScope
import dev.nucleusframework.composenativetray.tray.api.Tray
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

@Composable
fun Tray(
    icon: IconSource,
    tooltip: String,
    primaryAction: () -> Unit,
    menu: List<MenuItem>
) {
    val menuContent: @Composable ComposableTrayMenuScope.() -> Unit = {
        MenuContent(menu)
    }
    val shouldBeMonochrome = when (Platform.asDesktop()) {
        Platform.Desktop.MacOS -> true
        Platform.Desktop.Linux -> false
        Platform.Desktop.Windows -> false
    }
    if (shouldBeMonochrome && icon is IconSource.VectorIconSource) {
        // for tray icon the library automatically converts the ImageVector to monochrome
        // we want this behavior only for macOS
        Tray(
            icon = icon.value,
            tooltip = tooltip,
            primaryAction = primaryAction,
            menuContent = menuContent,
        )
    } else {
        Tray(
            icon = icon.rememberPainter(),
            tooltip = tooltip,
            primaryAction = primaryAction,
            menuContent = menuContent
        )
    }
}

@Composable
private fun ComposableTrayMenuScope.renderTrayItem(item: MenuItem) {
    when (item) {
        is MenuItem.SingleItem -> {
            RenderTraySingleItem(item)
        }

        is MenuItem.SubMenu -> {
            RenderTraySubMenu(item)
        }

        MenuItem.Separator -> Divider()
    }
}

@Composable
private fun ComposableTrayMenuScope.RenderTraySingleItem(item: MenuItem.SingleItem) {
    val title = item.title.collectAsState().value.rememberString()
    val isEnabled by item.isEnabled.collectAsState()
    val iconSource = item.icon.collectAsState().value
    val onClick = item::invoke
    when (iconSource) {
        is IconSource.VectorIconSource -> Item(
            label = title,
            isEnabled = isEnabled,
            onClick = onClick,
            icon = iconSource.value,
        )

        is IconSource.PainterIconSource -> Item(
            label = title,
            isEnabled = isEnabled,
            onClick = onClick,
            icon = iconSource.value,
        )

        null -> Item(
            label = title,
            isEnabled = isEnabled,
            onClick = onClick,
        )
    }
}

@Composable
private fun ComposableTrayMenuScope.MenuContent(menu: List<MenuItem>) {
    for (item in menu) {
        renderTrayItem(item)
    }
}

@Composable
private fun ComposableTrayMenuScope.RenderTraySubMenu(submenu: MenuItem.SubMenu) {
    val title = submenu.title.collectAsState().value.rememberString()
    val isEnabled by submenu.isEnabled.collectAsState()
    val iconSource = submenu.icon.collectAsState().value
    val menu = submenu.items.collectAsState().value
    val submenuContent: @Composable ComposableTrayMenuScope.() -> Unit = {
        MenuContent(menu)
    }
    when (iconSource) {
        is IconSource.PainterIconSource -> {
            SubMenu(
                label = title,
                isEnabled = isEnabled,
                submenuContent = submenuContent,
                icon = iconSource.value,
            )
        }

        is IconSource.VectorIconSource -> {
            SubMenu(
                label = title,
                isEnabled = isEnabled,
                submenuContent = submenuContent,
                icon = iconSource.value,
            )
        }

        null -> {
            SubMenu(
                label = title,
                isEnabled = isEnabled,
                submenuContent = submenuContent,
            )
        }
    }
}
