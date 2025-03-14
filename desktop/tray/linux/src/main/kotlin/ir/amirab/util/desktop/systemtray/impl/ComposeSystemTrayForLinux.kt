package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toAwtImage
import com.google.auto.service.AutoService
import dorkbox.systemTray.Menu
import dorkbox.systemTray.Separator
import dorkbox.systemTray.SystemTray
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.systemtray.IComposeSystemTray
import java.awt.Image

@AutoService(IComposeSystemTray::class)
class ComposeSystemTrayForLinux : IComposeSystemTray {
    @Composable
    override fun ComposeSystemTray(
        icon: IconSource,
        tooltip: StringSource,
        menu: List<MenuItem>,
        onClick: () -> Unit
    ) {
        val tooltipString = tooltip.rememberString()
        val systemTray: SystemTray? = remember { SystemTray.get(tooltipString) }
        if (systemTray == null) {
            System.err.println("System tray is not supported")
            return
        }

        val iconPainter = icon.rememberPainter()
        val awtImage = remember(iconPainter) {
            iconPainter.toAwtImage(
                GlobalDensity,
                GlobalLayoutDirection,
            )
        }

        LaunchedEffect(awtImage) {
            systemTray.setImage(awtImage)
        }
        LaunchedEffect(tooltipString) {
            systemTray.setTooltip(tooltipString)
        }

        val immutableMenu = menu.toImmutableMenuItem()
        DisposableEffect(immutableMenu) {
            systemTray.menu.addImmutableMenu(immutableMenu)
            onDispose {
                for (entry in systemTray.menu.entries) {
                    systemTray.menu.remove(entry)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                systemTray.shutdown()
            }
        }
    }
}

private fun Menu.addImmutableMenu(immutableMenu: List<ImmutableMenuItem>) {
    for (item in immutableMenu) {
        when (item) {
            ImmutableMenuItem.Separator -> add(Separator())
            is ImmutableMenuItem.SingleItem -> add(
                dorkbox.systemTray.MenuItem(
                    item.title,
                    item.icon,
                    {
                        item.onAction()
                    }
                )
            )

            is ImmutableMenuItem.SubMenu -> add(
                Menu(
                    item.title,
                    item.icon,
                ).apply {
                    addImmutableMenu(item.items)
                }
            )
        }
    }
}

@Composable
private fun List<MenuItem>.toImmutableMenuItem(): List<ImmutableMenuItem> {
    val density = GlobalDensity
    val layoutDirection = GlobalLayoutDirection
    return map {
        when (it) {
            MenuItem.Separator -> ImmutableMenuItem.Separator
            is MenuItem.SingleItem -> {
                ImmutableMenuItem.SingleItem(
                    title = it.title.collectAsState().value.rememberString(),
                    icon = null,
//                    icon = it.icon.collectAsState().value?.rememberPainter()?.toAwtImage(
//                        density, layoutDirection,
//                    ),
                    onAction = it::onClick
                )
            }

            is MenuItem.SubMenu -> ImmutableMenuItem.SubMenu(
                title = it.title.collectAsState().value.rememberString(),
                icon = it.icon.collectAsState().value?.rememberPainter()?.toAwtImage(
                    GlobalDensity, GlobalLayoutDirection,
                ),
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