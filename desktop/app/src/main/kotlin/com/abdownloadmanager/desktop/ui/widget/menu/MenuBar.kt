package com.abdownloadmanager.desktop.ui.widget.menu

import com.abdownloadmanager.desktop.actions.LocalShortCutManager
import com.abdownloadmanager.utils.compose.ProvideTextStyle
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.utils.KeyUtil
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.WithContentColor
import ir.amirab.util.compose.action.MenuItem
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import javax.swing.KeyStroke

enum class MenuDisabledItemBehavior {
    Filter,
    LowerOpacity,
}

val LocalMenuDisabledItemBehavior = compositionLocalOf {
    MenuDisabledItemBehavior.LowerOpacity
}
val LocalMenuBoxClip = compositionLocalOf<Shape> {
    RoundedCornerShape(6.dp)
}


@Composable
fun MenuBar(
    modifier: Modifier = Modifier,
    subMenuList: List<MenuItem.SubMenu>,
) {
    var openedItem: MenuItem.SubMenu? by remember {
        mutableStateOf(null)
    }
    val onRequestClose = {
        openedItem = null
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (subMenu in subMenuList) {
            val isSelected = openedItem == subMenu
            Column {
                Column(
                    modifier
                        .clickable {
                            openedItem = subMenu
                        }
                        .ifThen(isSelected) {
                            background(myColors.surface)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {
                    val text = subMenu.title.collectAsState().value
                    val (firstChar, leadingText) = remember(text) {
                        when (text.length) {
                            0 -> "" to ""
                            1 -> text to ""
                            else -> text.first().toString() to text.substring(1)
                        }
                    }
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append(firstChar)
                            }
                            append(leadingText)
                        },
                        maxLines = 1,
                        fontSize = myTextSizes.base,
                        color = myColors.onBackground,
                    )
                }
                if (isSelected) {
                    MyDropDown(
                        onDismissRequest = onRequestClose
                    ) {
                        CompositionLocalProvider(
                            LocalMenuBoxClip provides RectangleShape
                        ) {
                            SubMenu(subMenu, onRequestClose = onRequestClose)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SubMenu(
    subMenu: MenuItem.SubMenu,
    onRequestClose: () -> Unit,
    header: (@Composable () -> Unit)? = null,
) {
    SubMenu(
        subMenu = subMenu.items.collectAsState().value,
        header = header,
        onRequestClose = onRequestClose,
    )
}

@Composable
fun MenuColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = LocalMenuBoxClip.current
    Column(
        Modifier
            .shadow(24.dp)
//                .verticalScroll(rememberScrollState())
            .clip(shape)
            .width(IntrinsicSize.Max)
            .widthIn(120.dp)
            .border(1.dp, myColors.surface, shape)
            .background(myColors.menuGradientBackground)
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        content()
    }
}

@Composable
fun SubMenu(
    subMenu: List<MenuItem>,
    onRequestClose: () -> Unit,
    header: (@Composable () -> Unit)? = null,
) {
    var openedItem: MenuItem.SubMenu? by remember {
        mutableStateOf(null)
    }
    var lastHoveredItem by remember {
        mutableStateOf(null as MenuItem?)
    }

    WithContentColor(myColors.onMenuColor) {
        val shape = LocalMenuBoxClip.current
        Column(
            Modifier
                .shadow(24.dp)
//                .verticalScroll(rememberScrollState())
                .clip(shape)
                .width(IntrinsicSize.Max)
                .widthIn(120.dp)
                .border(1.dp, myColors.surface, shape)
                .background(myColors.menuGradientBackground)
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            header?.invoke()
            for (menuItem in subMenu) {
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                LaunchedEffect(isHovered) {
                    if (isHovered) {
//                        println("last overed item is ${menuItem.hashCode()}")
//                        println("last overed item is ${(menuItem as? MenuItem.SubMenu)?.title?.value}")
                        lastHoveredItem = menuItem
                    }
                }
                RenderMenuItem(
                    menuItem = menuItem,
                    openedItem = openedItem,
                    onRequestCLose = onRequestClose,
                    isSelected = openedItem == menuItem,
                    onRequestOpenItem = {
                        openedItem = it
                    },
                    isHovered = lastHoveredItem == menuItem,
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
    val iconModifier = Modifier.size(16.dp)
    val title by item.title.collectAsState()
    val icon by item.icon.collectAsState()
    val itemPadding = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isEnabled = (item as? MenuItem.HasEnable)
        ?.isEnabled
        ?.collectAsState()
        ?.value ?: true
    Row(modifier
        .ifThen(!isEnabled) { alpha(0.5f) }
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
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(iconModifier)
            }
        }
        Text(
            title,
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
    openedItem: MenuItem.SubMenu?,
    onRequestCLose: () -> Unit,
    isSelected: Boolean,
    isHovered: Boolean,
    modifier: Modifier = Modifier,
    onRequestOpenItem: (MenuItem.SubMenu?) -> Unit,
) {
//    val isEnabled by menuItem.isEnabled.collectAsState()
    LaunchedEffect(isHovered, menuItem) {
        if (isHovered) {
            if (menuItem is MenuItem.SubMenu) {
                onRequestOpenItem(menuItem)
            } else {
                onRequestOpenItem(null)
            }
        }
    }
    Row(
        modifier
            .fillMaxWidth()
    ) {
        when (menuItem) {
            MenuItem.Separator -> {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(myColors.onSurface / 5)
                )
            }

            is MenuItem.SingleItem -> {
                RenderSingleItem(
                    item = menuItem,
                    isSelected = isSelected,
                    onRequestClose = onRequestCLose,
                )
            }

            is MenuItem.SubMenu -> {
                RenderSubMenuItem(
                    menuItem = menuItem,
                    isSelected = isSelected,
                    onRequestCLose = onRequestCLose,
                    openedItem = openedItem,
                    onRequestOpenItem = onRequestOpenItem,
                )
            }
        }
    }
}

@Composable
fun RenderSubMenuItem(
    menuItem: MenuItem.SubMenu,
    isSelected: Boolean,
    openedItem: MenuItem.SubMenu?,
    onRequestOpenItem: (MenuItem.SubMenu?) -> Unit,
    onRequestCLose: () -> Unit,
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
                Modifier.size(16.dp),
            )
        })
    if (openedItem == menuItem) {
        SiblingDropDown(
            onDismissRequest = {
                onRequestOpenItem(null)
            }
        ) {
            SubMenu(menuItem, onRequestCLose)
        }
    }
}

@Composable
fun RenderSingleItem(
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
fun RenderShortcutStroke(shortcutStroke: KeyStroke) {
    val modifiers = remember(shortcutStroke) {
        buildList {
            addAll(KeyUtil.getModifiers(shortcutStroke.modifiers))
            add(KeyUtil.getKeyText(shortcutStroke.keyCode))
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
            for (it in modifiers) {
                WithContentAlpha(0.75f) {
                    WithContentColor(myColors.onBackground) {
                        Text(
                            it,
                            Modifier
                                .clip(shape)
                                .background(myColors.onBackground / 5)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}