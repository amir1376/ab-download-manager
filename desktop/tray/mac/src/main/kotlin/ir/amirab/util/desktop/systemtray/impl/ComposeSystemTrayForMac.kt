package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.desktop.window.moveSafe
import com.abdownloadmanager.shared.ui.widget.menu.SubMenu
import com.google.auto.service.AutoService
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.systemtray.IComposeSystemTray
import java.awt.Image
import java.awt.SystemTray

@AutoService(IComposeSystemTray::class)
class ComposeSystemTrayForMac : IComposeSystemTray {
    @Composable
    override fun ComposeSystemTray(
        icon: IconSource,
        tooltip: StringSource,
        menu: List<MenuItem>,
        onClick: () -> Unit
    ) {
        AwtTray(
            tooltip = tooltip.rememberString(),
            icon = icon.rememberPainter(),
            menuItems = menu.toImmutableMenuItem(),
        )
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
//                    icon = it.icon.collectAsState().value?.rememberPainter()?.toAwtImage(
//                        density, layoutDirection,
//                    ),
                    icon = null,
                    onAction = it::onClick
                )
            }

            is MenuItem.SubMenu -> ImmutableMenuItem.SubMenu(
                title = it.title.collectAsState().value.rememberString(),
                icon = null,
                items = it.items.collectAsState().value.toImmutableMenuItem()
            )
        }
    }
}


@Immutable
internal sealed class ImmutableMenuItem {
    data object Separator : ImmutableMenuItem()
    data class SingleItem(
        val title: String,
        val icon: Image?,
        val onAction: () -> Unit,
    ) : ImmutableMenuItem()

    data class SubMenu(
        val title: String,
        val icon: Image?,
        val items: List<ImmutableMenuItem>
    ) : ImmutableMenuItem()
}