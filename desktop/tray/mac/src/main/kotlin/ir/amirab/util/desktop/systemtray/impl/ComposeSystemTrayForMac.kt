package ir.amirab.util.desktop.systemtray.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.auto.service.AutoService
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.desktop.systemtray.IComposeSystemTray
import java.awt.Image

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
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return map {
        when (it) {
            MenuItem.Separator -> ImmutableMenuItem.Separator
            is MenuItem.SingleItem -> {
                ImmutableMenuItem.SingleItem(
                    title = it.title.collectAsState().value.rememberString(),
                    icon = it.icon.collectAsState().value?.rememberPainter()?.toAwtImage(
                        density, layoutDirection,
                    ),
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