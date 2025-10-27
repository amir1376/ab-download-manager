package com.abdownloadmanager.shared.ui.widget.menu.native

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import com.abdownloadmanager.shared.util.LocalShortCutManager
import com.abdownloadmanager.shared.util.PlatformKeyStroke
import com.abdownloadmanager.shared.util.ShortcutManager
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.action.MenuItem

@Composable
fun NativeMenuBar(
    scope: FrameWindowScope,
    subMenuList: List<MenuItem.SubMenu>
) {
    val shortcutManager = LocalShortCutManager.current

    scope.MenuBar {
        subMenuList.forEach { item ->
            val items by item.items.collectAsState()
            val title by item.title.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            Menu(title.rememberString(), enabled = enabled) {
                items.forEach { renderMenuItem(it, shortcutManager) }
            }
        }
    }
}

@Composable
private fun MenuScope.renderMenuItem(item: MenuItem, shortcutManager: ShortcutManager?) {
    when (item) {
        is MenuItem.SubMenu -> {
            val items by item.items.collectAsState()
            val title by item.title.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            Menu(title.rememberString(), enabled = enabled) {
                items.forEach { renderMenuItem(it, shortcutManager) }
            }
        }

        is MenuItem.Separator -> Separator()
        is MenuItem.SingleItem -> {
            val title by item.title.collectAsState()
            val icon by item.icon.collectAsState()
            val enabled by item.isEnabled.collectAsState()
            val shortcut = remember(shortcutManager, item) {
                shortcutManager?.getShortCutOf(item)?.toKeyShortcut()
            }
            Item(
                title.rememberString(),
                onClick = item::onClick,
                icon = icon?.suitablePainterForMenu(),
                enabled = enabled,
                shortcut = shortcut
            )
        }
    }
}


@Composable
private fun IconSource.suitablePainterForMenu(): Painter {
    val isLight = !isSystemInDarkTheme()
    return if (isLight && requiredTint) {
        val painter = rememberPainter()
        remember(painter) {
            painter.withTint(Color.Black)
        }
    } else rememberPainter()
}

fun Painter.withTint(tint: Color): Painter = object : Painter() {
    override val intrinsicSize = this@withTint.intrinsicSize

    override fun DrawScope.onDraw() {
        with(this@withTint) {
            draw(size = size, colorFilter = ColorFilter.tint(tint))
        }
    }
}


private fun PlatformKeyStroke.toKeyShortcut(): KeyShortcut {
    val mods = getModifiers().map { it.trim() }

    return KeyShortcut(
        key = Key(keyCode),
        ctrl = mods.any { it in listOf("⌃", "ctrl", "control") },
        alt = mods.any { it in listOf("⌥", "alt", "option") },
        shift = mods.any { it in listOf("⇧", "shift") },
        meta = mods.any { it in listOf("⌘", "meta", "command") }
    )
}
