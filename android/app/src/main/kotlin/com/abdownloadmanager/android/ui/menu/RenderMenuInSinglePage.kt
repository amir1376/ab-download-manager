package com.abdownloadmanager.android.ui.menu

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.menu.custom.LocalMenuBoxClip
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.localizationmanager.WithLanguageDirection
import ir.amirab.util.compose.modifiers.autoMirror

@Composable
private fun RenderMenuInSinglePage(
    menuStack: StackMenuState,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    val shape = LocalMenuBoxClip.current
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f)
    }
    WithLanguageDirection {
        Column(
            modifier
                .shadow(4.dp, shape)
                .clip(shape)
                .widthIn(200.dp)
                .border(1.dp, myColors.onSurface / 0.1f, shape)
                .background(myColors.surface)
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            BaseStackedMenu(
                menuStack,
                onDismissRequest,
            ) { currentMenu, render ->
                val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                render()
                val currentTitle = currentMenu.title.collectAsState().value.rememberString()
                if (currentTitle.isNotEmpty()) {
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

@Composable
fun RenderMenuInSinglePage(
    menu: List<MenuItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    RenderMenuInSinglePage(
        menuStack = rememberMenuStack(menu),
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
        menuStack = rememberMenuStack(menu),
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
}


