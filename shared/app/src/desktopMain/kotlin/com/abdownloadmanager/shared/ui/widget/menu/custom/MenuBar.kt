package com.abdownloadmanager.shared.ui.widget.menu.custom

import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import ir.amirab.util.compose.action.MenuItem
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

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
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            LaunchedEffect(isHovered) {
                if (isHovered && openedItem != null) {
                    openedItem = subMenu
                }
            }
            Column {
                Column(
                    modifier
                        .hoverable(interactionSource)
                        .clickable {
                            openedItem = subMenu
                        }
                        .ifThen(isSelected) {
                            background(myColors.surface)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {
                    val text = subMenu.title.collectAsState().value.rememberString()
                    Text(
                        text = text,
                        maxLines = 1,
                        fontSize = myTextSizes.base,
                        color = myColors.onBackground,
                    )
                }
                if (isSelected) {
                    MyDropDown(
                        onDismissRequest = onRequestClose,
                        focusable = false,
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
