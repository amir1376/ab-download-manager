package com.abdownloadmanager.android.pages.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.RenderControlSelections
import com.abdownloadmanager.android.ui.SelectionControlButton
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.alphaFlicker
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.ifThen

@Immutable
data class OpenOptionMenuProps(
    val subMenu: MenuItem.SubMenu,
    val layoutCoordinates: LayoutCoordinates,
)

@Composable
fun SelectionPopup(
    modifier: Modifier,
    options: List<MenuItem>,
    onRequestSelectAll: () -> Unit,
    onRequestSelectInside: () -> Unit,
    onRequestInvertSelection: () -> Unit,
    selectionCount: Int,
    total: Int,
    onRequestClose: () -> Unit,
    renderSubMenu: @Composable (menu: OpenOptionMenuProps?, close: () -> Unit) -> Unit
) {
    var submenuToOpen: OpenOptionMenuProps? by remember { mutableStateOf(null) }
    val dismissExtraMenu = {
        submenuToOpen = null
    }
    val shape = myShapes.defaultRounded
    Column(
        modifier
            .padding(16.dp)
            .shadow(4.dp, shape)
            .clip(shape)
            .border(1.dp, myColors.onSurface / 0.1f, shape)
            .background(myColors.surface),
    ) {
        AnimatedVisibility(
            submenuToOpen == null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            RenderDownloadControlSelections(
                onRequestSelectAll = onRequestSelectAll,
                onRequestSelectInside = onRequestSelectInside,
                onRequestInvertSelection = onRequestInvertSelection,
                selectionCount = selectionCount,
                total = total,
                onRequestClose = onRequestClose,
            )
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.1f)
        )
        RenderSelectionMenuActions(options, {
            submenuToOpen = it
        })
    }
    renderSubMenu(submenuToOpen, dismissExtraMenu)
}

@Composable
fun RenderDownloadControlSelections(
    onRequestSelectAll: () -> Unit,
    onRequestSelectInside: () -> Unit,
    onRequestInvertSelection: () -> Unit,
    onRequestClose: () -> Unit,
    selectionCount: Int,
    total: Int,
) {
    RenderControlSelections(
        onRequestSelectAll = onRequestSelectAll,
        onRequestSelectInside = onRequestSelectInside,
        onRequestInvertSelection = onRequestInvertSelection,
        selectionCount = selectionCount,
        total = total,
        otherActions = {
            SelectionControlButton(
                icon = MyIcons.close,
                contentDescription = Res.string.close.asStringSource(),
                modifier = Modifier,
                enabled = true,
                toggledOff = false,
                onClick = {
                    onRequestClose()
                },
            )
        }
    )
}

@Composable
fun RenderSelectionMenuActions(
    options: List<MenuItem>,
    onRequestOpenSubmenu: (OpenOptionMenuProps) -> Unit,
) {
    Row(
        Modifier.height(IntrinsicSize.Max)
    ) {
        val reactableItemModifier = Modifier
            .weight(1f)
        for (action in options) {
            when (action) {
                MenuItem.Separator -> {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(myColors.onBackground / 0.2f)
                    )
                }

                is MenuItem.SingleItem -> {
                    val icon = action.icon.collectAsState().value
                    VerticalMenuOption(
                        title = action.title.collectAsState().value,
                        icon = requireNotNull(icon) {
                            "use an action that has icon in the HorizontalMenu"
                        },
                        enabled = action.isEnabled.collectAsState().value,
                        onClick = {
                            action()
                        },
                        modifier = reactableItemModifier,
                    )
                }

                is MenuItem.SubMenu -> {
                    val icon = action.icon.collectAsState().value
                    VerticalMenuOption(
                        title = action.title.collectAsState().value,
                        icon = requireNotNull(icon) {
                            "use an action that has icon in the HorizontalMenu"
                        },
                        enabled = action.isEnabled.collectAsState().value,
                        onClick = {
                            onRequestOpenSubmenu(OpenOptionMenuProps(action, it))
                        },
                        modifier = reactableItemModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalMenuOption(
    title: StringSource,
    icon: IconSource,
    enabled: Boolean,
    onClick: (LayoutCoordinates) -> Unit,
    modifier: Modifier,
) {
    SelectionActionButton(
        icon,
        contentDescription = title.rememberString(),
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        size = 24.dp,
        padding = PaddingValues(12.dp),
    )
}


@Composable
private fun SelectionActionButton(
    icon: IconSource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    indicateActive: Boolean = false,
    requiresAttention: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (LayoutCoordinates) -> Unit,
    padding: PaddingValues,
    size: Dp,
    shape: Shape = RectangleShape,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isActiveOrFocused = indicateActive || isFocused
    var layoutCoordinates by remember { mutableStateOf(null as LayoutCoordinates?) }
    Box(
        modifier
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .ifThen(isActiveOrFocused || requiresAttention) {
                border(
                    1.dp,
                    myColors.focusedBorderColor / if (isActiveOrFocused) 1f else alphaFlicker(),
                    shape
                )
            }
            .clip(shape)
            .onGloballyPositioned {
                layoutCoordinates = it
            }
            .clickable(
                enabled = enabled,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = {
                    layoutCoordinates?.let(onClick)
                },
            )
            .padding(padding)
            .wrapContentSize()
    ) {
        MyIcon(
            icon,
            contentDescription,
            Modifier
                .size(size)
        )
    }
}
