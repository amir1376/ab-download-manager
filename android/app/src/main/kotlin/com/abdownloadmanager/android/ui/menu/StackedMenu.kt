package com.abdownloadmanager.android.ui.menu

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource

@Composable
fun rememberMenuStack(
    menu: MenuItem.SubMenu,
): StackMenuState {
    return remember(menu) {
        StackMenuState(mutableStateListOf(menu))
    }
}

@Composable
fun rememberMenuStack(
    menu: List<MenuItem>,
): StackMenuState {
    return remember(menu) {
        StackMenuState(
            mutableStateListOf(
                MenuItem.SubMenu(
                    title = "".asStringSource(),
                    items = menu,
                )
            )
        )
    }
}

@Stable
class StackMenuState(val menu: SnapshotStateList<MenuItem.SubMenu>) {
    val menuStack = menu
    val currentMenu by derivedStateOf {
        menuStack.last()
    }
    val size by derivedStateOf {
        menu.size
    }

    fun push(newMenu: MenuItem.SubMenu) {
        menu.add(newMenu)
    }
    val canGoBack by derivedStateOf {
        menuStack.size > 1
    }
    fun pop(): Boolean {
        if (menuStack.size == 1) {
            return false
        } else {
            menuStack.removeAt(menuStack.lastIndex)
            return true
        }
    }
}

@Composable
fun BaseStackedMenu(
    menuStack: StackMenuState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    menuWrapper: (@Composable (
        subMenu: MenuItem.SubMenu,
        renderMenu: @Composable () -> Unit
    ) -> Unit) = @Composable { _, render -> render() }
) {
    BackHandler {
        val menuStack = menuStack
        if (!menuStack.pop()) {
            onDismissRequest()
        }
    }
    val currentMenu = menuStack.currentMenu
    AnimatedContent(
        currentMenu
    ) { currentMenu ->
        Column {
            menuWrapper(currentMenu) {
                Menu(
                    menu = currentMenu,
                    onNewMenuSelected = { newMenu ->
                        menuStack.push(newMenu)
                    },
                    onRequestClose = {
                        onDismissRequest()
                    },
                    modifier = modifier
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}
