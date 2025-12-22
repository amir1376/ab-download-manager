package com.abdownloadmanager.android.ui.menu

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.menu.custom.LocalMenuBoxClip
import com.abdownloadmanager.shared.ui.widget.menu.custom.LocalMenuDisabledItemBehavior
import com.abdownloadmanager.shared.ui.widget.menu.custom.MenuDisabledItemBehavior
import com.abdownloadmanager.shared.util.LocalShortCutManager
import com.abdownloadmanager.shared.util.PlatformKeyStroke
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.ProvideTextStyle
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.localizationmanager.WithLanguageDirection
import ir.amirab.util.compose.modifiers.autoMirror
import ir.amirab.util.ifThen

@Composable
fun RenderMenuInSinglePage(
    menuStack: SnapshotStateList<MenuItem.SubMenu>,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    val shape = LocalMenuBoxClip.current
    val currentMenu = menuStack.last()
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f)
    }
    BackHandler {
        if (menuStack.size == 1) {
            onDismissRequest()
        } else {
            menuStack.removeAt(menuStack.lastIndex)
        }
    }
    WithLanguageDirection {
        Box(
            modifier
                .shadow(4.dp, shape)
                .clip(shape)
                .widthIn(200.dp)
                .border(1.dp, myColors.onSurface / 0.1f, shape)
                .background(myColors.surface)
        ) {
            AnimatedContent(
                currentMenu
            ) { currentMenu ->
                Column {
                    Menu(
                        menu = currentMenu,
                        onNewMenuSelected = { newMenu ->
                            menuStack.add(newMenu)
                        },
                        onRequestClose = {
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .verticalScroll(rememberScrollState()),
                    )
                    val currentTitle = currentMenu.title.collectAsState().value.rememberString()
                    if (currentTitle.isNotEmpty()) {
                        val onBackPressedDispatcher =
                            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                        RenderSeparator()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(
                                    enabled = menuStack.size > 1
                                ) {
                                    onBackPressedDispatcher?.onBackPressed()
                                }
                                .fillMaxWidth()
                                .heightIn(mySpacings.thumbSize)
                                .padding(horizontal = 16.dp)
                        ) {
                            val iconModifier = Modifier
                                .size(menuIconSize)
                            if (menuStack.size > 1) {
                                MyIcon(
                                    MyIcons.back,
                                    null,
                                    iconModifier.autoMirror(),
                                )
                                Spacer(Modifier.width(16.dp))
                            }
                            Text(
                                currentTitle,
                                Modifier.weight(1f),
                                color = LocalContentColor.current / 0.75f,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderMenuInSinglePage(
    menu: List<MenuItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    RenderMenuInSinglePage(
        menuStack = remember(menu) {
            mutableStateListOf(
                MenuItem.SubMenu(
                    title = "".asStringSource(),
                    items = menu,
                )
            )
        },
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
fun RenderMenuInSinglePage(
    menu: MenuItem.SubMenu,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    RenderMenuInSinglePage(
        menuStack = remember(menu) {
            mutableStateListOf(menu)
        },
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
}

//


/**
 * render a menu
 */
@Composable
private fun Menu(
    menu: MenuItem.SubMenu,
    onRequestClose: () -> Unit,
    onNewMenuSelected: (MenuItem.SubMenu) -> Unit,
    modifier: Modifier,
) {
    var openedItem: MenuItem.SubMenu? by remember {
        mutableStateOf(null)
    }

    WithContentColor(myColors.onMenuColor) {
        Column(
            modifier
        ) {
            val items by menu.items.collectAsState()
            for (menuItem in items) {
                val interactionSource = remember { MutableInteractionSource() }
                RenderMenuItem(
                    menuItem = menuItem,
//                    openedItem = openedItem,
                    onRequestClose = onRequestClose,
                    isSelected = openedItem == menuItem,
                    onRequestOpenItem = {
                        onNewMenuSelected(it)
                    },
                    modifier = Modifier.hoverable(interactionSource)
                )
            }
        }
    }
}

@Composable
private fun ReactableItem(
    item: MenuItem.ReadableItem,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    extraContent: @Composable () -> Unit = {},
) {
    val iconModifier = Modifier.size(menuIconSize)
    val title by item.title.collectAsState()
    val icon by item.icon.collectAsState()
    val itemPadding = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isEnabled = (item as? MenuItem.HasEnable)
        ?.isEnabled
        ?.collectAsState()
        ?.value ?: true
    Row(
        modifier
            .ifThen(!isEnabled) { alpha(0.5f) }
            .heightIn(mySpacings.thumbSize)
            .hoverable(interactionSource)
            .background(
                when {
                    (isHovered && isEnabled) || isSelected -> {
                        myColors.surface
                    }

                    else -> {
                        Color.Transparent
                    }
                }
            )
            .clickable(enabled = isEnabled) {
                onClick()
            }
            .then(itemPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon.let { icon ->
            if (icon != null) {
                Spacer(Modifier.width(4.dp))
                MyIcon(icon, null, iconModifier)
                Spacer(Modifier.width(16.dp))
            } else {
                Spacer(iconModifier)
            }
        }
        Text(
            title.rememberString(),
            Modifier.weight(1f),
            fontSize = myTextSizes.base,
            softWrap = false,
            maxLines = 1,
        )
        Spacer(Modifier.width(16.dp))
        extraContent()
    }
}

@Composable
private fun RenderMenuItem(
    menuItem: MenuItem,
    onRequestClose: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onRequestOpenItem: (MenuItem.SubMenu) -> Unit,
) {
//    val isEnabled by menuItem.isEnabled.collectAsState()
    Row(
        modifier
            .fillMaxWidth()
    ) {
        when (menuItem) {
            MenuItem.Separator -> {
                RenderSeparator()
            }

            is MenuItem.SingleItem -> {
                RenderSingleItem(
                    item = menuItem,
                    isSelected = isSelected,
                    onRequestClose = onRequestClose,
                )
            }

            is MenuItem.SubMenu -> {
                RenderSubMenuItem(
                    menuItem = menuItem,
                    isSelected = isSelected,
//                    onRequestCLose = onRequestCLose,
//                    openedItem = openedItem,
                    onRequestOpenItem = onRequestOpenItem,
                )
            }
        }
    }
}

@Composable
fun RenderSeparator() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(myColors.onSurface / 5)
    )
}

@Composable
private fun RenderSubMenuItem(
    menuItem: MenuItem.SubMenu,
    isSelected: Boolean,
//    openedItem: MenuItem.SubMenu?,
    onRequestOpenItem: (MenuItem.SubMenu) -> Unit,
//    onRequestCLose: () -> Unit,
) {
    ReactableItem(
        item = menuItem,
        onClick = {
            onRequestOpenItem(menuItem)
        },
        isSelected = isSelected,
        extraContent = {
            MyIcon(
                MyIcons.next,
                null,
                Modifier
                    .size(16.dp)
                    .autoMirror(),
            )
        })
//    if (openedItem == menuItem) {
//        SiblingDropDown(
//            onDismissRequest = {
//                onRequestOpenItem(null)
//            }
//        ) {
//            SubMenu(menuItem, onRequestCLose)
//        }
//    }
}

@Composable
private fun RenderSingleItem(
    onRequestClose: () -> Unit,
    isSelected: Boolean,
    item: MenuItem.SingleItem,
) {
    val isEnabled by item.isEnabled.collectAsState()
    if (!isEnabled && LocalMenuDisabledItemBehavior.current == MenuDisabledItemBehavior.Filter) {
        return
    }

    val shortcutManager = LocalShortCutManager.current
    val shortcutStroke = remember(shortcutManager, item) {
        shortcutManager?.getShortCutOf(item)
    }
    val onClick = {
        if (item.shouldDismissOnClick) {
            onRequestClose()
        }
        item.onClick()
    }
    ReactableItem(
        item = item,
        onClick = onClick,
        isSelected = isSelected,
        extraContent = {
            if (shortcutStroke != null) {
                RenderShortcutStroke(shortcutStroke)
            }
        }
    )

}

@Composable
private fun RenderShortcutStroke(shortcutStroke: PlatformKeyStroke) {
    val modifiers = remember(shortcutStroke) {
        buildList {
            addAll(shortcutStroke.getModifiers())
            add(shortcutStroke.getKeyText())
        }
    }
    ProvideTextStyle(
        TextStyle(
            fontSize = myTextSizes.xs,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            val shape = RoundedCornerShape(10)
            WithContentColor(myColors.onBackground) {
                Text(
                    modifiers.joinToString("+"),
                    Modifier
                        .clip(shape)
                        .background(myColors.onBackground / 5)
                        .padding(2.dp)
                )
            }
        }
    }
}

private val menuIconSize = 20.dp
