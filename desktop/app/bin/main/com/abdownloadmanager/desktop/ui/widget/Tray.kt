package com.abdownloadmanager.desktop.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.ApplicationScope
import com.kdroid.composetray.menu.api.TrayMenuBuilder
import com.kdroid.composetray.tray.api.Tray
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

@Composable
fun ApplicationScope.Tray(
    icon: IconSource,
    tooltip: String,
    primaryAction: () -> Unit,
    menu: List<MenuItem>
) {
    // use this composable to properly update menu item properties (State<T>,StateFlow<T> properties)
    val immutableMenu = menu.toImmutableMenuItem()
    val menuContent: TrayMenuBuilder.() -> Unit = {
        for (item in immutableMenu) {
            renderTrayItem(item)
        }
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

private fun TrayMenuBuilder.renderTrayItem(item: ImmutableMenuItem) {
    when (item) {
        is ImmutableMenuItem.SingleItem -> {
            renderTraySingleItem(item)
        }

        is ImmutableMenuItem.SubMenu -> {
            renderTraySubMenu(item)
        }

        is ImmutableMenuItem.Separator -> Divider()
    }
}

private fun TrayMenuBuilder.renderTraySingleItem(item: ImmutableMenuItem.SingleItem) {
    val title = item.title
    val isEnabled = item.enabled
    val onClick = item.onAction
    when (val iconSource = item.icon) {
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

private fun TrayMenuBuilder.renderTraySubMenu(submenu: ImmutableMenuItem.SubMenu) {
    val title = submenu.title
    val isEnabled = submenu.enabled
    val submenuContent: TrayMenuBuilder.() -> Unit = {
        for (item in submenu.items) {
            renderTrayItem(item)
        }
    }
    when (val iconSource = submenu.icon) {
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

@Composable
private fun List<MenuItem>.toImmutableMenuItem(): List<ImmutableMenuItem> {
    return map {
        when (it) {
            MenuItem.Separator -> ImmutableMenuItem.Separator
            is MenuItem.SingleItem -> {
                ImmutableMenuItem.SingleItem(
                    title = it.title.collectAsState().value.rememberString(),
                    icon = it.icon.collectAsState().value,
                    enabled = it.isEnabled.value,
                    onAction = it::onClick
                )
            }

            is MenuItem.SubMenu -> ImmutableMenuItem.SubMenu(
                title = it.title.collectAsState().value.rememberString(),
                icon = it.icon.value,
                enabled = it.isEnabled.value,
                items = it.items.collectAsState().value.toImmutableMenuItem()
            )
        }
    }
}


@Immutable
private sealed class ImmutableMenuItem {
    data object Separator : ImmutableMenuItem()
    data class SingleItem(
        val title: String,
        val icon: IconSource?,
        val enabled: Boolean,
        val onAction: () -> Unit,
    ) : ImmutableMenuItem()

    data class SubMenu(
        val title: String,
        val icon: IconSource?,
        val enabled: Boolean,
        val items: List<ImmutableMenuItem>
    ) : ImmutableMenuItem()
}
