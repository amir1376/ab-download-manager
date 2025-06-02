package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.menu.custom.MyDropDown
import com.abdownloadmanager.shared.ui.widget.menu.custom.SubMenu
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import ir.amirab.util.compose.action.MenuItem
import com.abdownloadmanager.shared.utils.div
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.Tooltip
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource

@Composable
fun Actions(
    list: List<MenuItem>,
    showLabels: Boolean,
) {
    val scrollState = rememberScrollState()
    Column {
        Row(
            Modifier
                .height(IntrinsicSize.Max)
                .horizontalScroll(scrollState)
        ) {
            for (a in list) {
                when (a) {
                    MenuItem.Separator -> {
                        Spacer(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp)
                                .width(1.dp)
                                .background(myColors.onBackground / 5)
                        )
                    }

                    is MenuItem.SingleItem -> {
                        ActionButton(Modifier, a, showLabels)
                    }

                    is MenuItem.SubMenu -> {
                        GroupActionButton(Modifier, a, showLabels)
                    }
                }
            }
        }
        val adapter = rememberScrollbarAdapter(scrollState)
        if (adapter.needScroll()) {
            HorizontalScrollbar(
                adapter = adapter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                style = LocalScrollbarStyle.current.copy(
                    thickness = 6.dp
                ),
            )
        }
    }
}

private fun androidx.compose.foundation.v2.ScrollbarAdapter.needScroll(): Boolean {
    return contentSize > viewportSize
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    action: MenuItem.SingleItem,
    showLabels: Boolean,
) {
    val enabled by action.isEnabled.collectAsState()
    Column(modifier) {
        ActionIconWithLabel(
            title = action.title.collectAsState().value,
            icon = action.icon.collectAsState().value,
            showLabels = showLabels,
            onClick = {
                action()
            },
            enabled = enabled
        )
    }
}

@Composable
private fun GroupActionButton(
    modifier: Modifier = Modifier,
    action: MenuItem.SubMenu,
    showLabels: Boolean,
) {
    val enabled by action.isEnabled.collectAsState()
    var showSubMenu by remember { mutableStateOf(false) }
    Column(modifier) {
        ActionIconWithLabel(
            title = action.title.collectAsState().value,
            icon = action.icon.collectAsState().value,
            showLabels = showLabels,
            onClick = {
                showSubMenu = !showSubMenu
            },
            enabled = enabled
        )
        val close = {
            showSubMenu = false
        }
        if (enabled && showSubMenu) {
            MyDropDown(onDismissRequest = close) {
                val items by action.items.collectAsState()
                SubMenu(subMenu = items, onRequestClose = close)
            }
        }
    }
}

@Composable
private fun ActionIconWithLabel(
    title: StringSource,
    icon: IconSource?,
    showLabels: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    OptionalTooltip(
        title.takeIf { !showLabels },
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onClick)
                .ifThen(!enabled) {
                    alpha(0.5f)
                }
                .padding(if (showLabels) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WithContentColor(myColors.onBackground) {
                WithContentAlpha(1f) {
                    icon?.let {
                        val iconSize = if (showLabels) 16.dp else 24.dp
                        MyIcon(
                            icon = it,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    if (showLabels) {
                        Spacer(Modifier.size(2.dp))
                        Text(title.rememberString(), maxLines = 1, fontSize = myTextSizes.sm)
                    }
                }
            }
        }
    }

}

@Composable
private fun OptionalTooltip(
    tooltip: StringSource?,
    content: @Composable () -> Unit
) {
    if (tooltip != null) {
        Tooltip(tooltip) {
            content()
        }
    } else {
        content()
    }
}
