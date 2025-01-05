package com.abdownloadmanager.desktop.pages.home

import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.menu.MyDropDown
import com.abdownloadmanager.shared.ui.widget.menu.SubMenu
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

@Composable
fun Actions(list: List<MenuItem>) {
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
                        ActionButton(Modifier, a)
                    }

                    is MenuItem.SubMenu -> {
                        GroupActionButton(Modifier, a)
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
) {
    val enabled by action.isEnabled.collectAsState()
    Column(
        modifier = modifier
            .clickable(
                enabled = enabled
            ) { action() }
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WithContentColor(myColors.onBackground) {
            WithContentAlpha(1f) {
                val icon by action.icon.collectAsState()
                val title by action.title.collectAsState()
                icon?.let {
                    MyIcon(it, null, Modifier.size(16.dp))
                }
                Spacer(Modifier.size(2.dp))
                Text(title.rememberString(), maxLines = 1, fontSize = myTextSizes.sm)
            }
        }
    }
}

@Composable
private fun GroupActionButton(
    modifier: Modifier = Modifier,
    action: MenuItem.SubMenu,
) {
    val enabled by action.isEnabled.collectAsState()
    var showSubMenu by remember { mutableStateOf(false) }
    Column(modifier) {
        Column(
            modifier = Modifier
                .clickable(enabled = enabled) {
                    showSubMenu = !showSubMenu
                }
                .ifThen(!enabled) {
                    alpha(0.5f)
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WithContentColor(myColors.onBackground) {
                WithContentAlpha(1f) {
                    val icon by action.icon.collectAsState()
                    val title by action.title.collectAsState()
                    icon?.let {
                        MyIcon(it, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.size(2.dp))
                    Text(title.rememberString(), maxLines = 1, fontSize = myTextSizes.sm)
                }
            }
        }
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
