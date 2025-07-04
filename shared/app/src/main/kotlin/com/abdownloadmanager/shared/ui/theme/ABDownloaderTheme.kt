package com.abdownloadmanager.shared.ui.theme

import ir.amirab.util.compose.action.buildMenu
import com.abdownloadmanager.shared.utils.darker
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberPopupPositionProviderAtPosition
import com.abdownloadmanager.shared.ui.widget.menu.custom.SubMenu
import com.abdownloadmanager.shared.utils.ui.*
import com.abdownloadmanager.shared.utils.ui.theme.*
import com.abdownloadmanager.shared.utils.ui.theme.UiScaledContent
import ir.amirab.util.compose.asStringSource

/*
fun MyColors.asMaterial2Colors(): Colors {
    return Colors(
        primary = primary,
        primaryVariant = primaryVariant,
        secondary = secondary,
        secondaryVariant = secondaryVariant,
        background = background,
        surface = surface,
        error = error,
        onPrimary = onPrimary,
        onSecondary = onSecondary,
        onBackground = onBackground,
        onSurface = onSurface,
        onError = onError,
        isLight = isLight
    )
}
*/



private val textSizes = TextSizes(
    xs = 8.sp,
    sm = 10.sp,
    base = 12.sp,
    lg = 14.sp,
    xl = 16.sp,
)

@Composable
fun ABDownloaderTheme(
    myColors: MyColors,
    uiScale: Float? = null,
    content: @Composable () -> Unit,
) {
    val systemDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalMyColors provides AnimatedColors(myColors, tween(500)),
        LocalUiScale provides uiScale,
        LocalSystemDensity provides systemDensity,
    ) {
        CompositionLocalProvider(
            LocalContextMenuRepresentation provides myContextMenuRepresentation(),
            LocalScrollbarStyle provides myDefaultScrollBarStyle(),
            LocalIndication provides ripple(),
            LocalContentColor provides myColors.onBackground,
            LocalContentAlpha provides 1f,
            LocalTextSizes provides textSizes,
            LocalTextStyle provides LocalTextStyle.current.copy(
                lineHeight = TextUnit.Unspecified,
                fontSize = textSizes.base,
            ),
        ) {
            // it is overridden by [Window] Composable,
            // but I put this here. maybe I need this outside of window  scope!
            UiScaledContent {
                content()
            }
        }
    }
}

private class MyContextMenuRepresentation : ContextMenuRepresentation {
    @Composable
    override fun Representation(state: ContextMenuState, items: () -> List<ContextMenuItem>) {
        val status = state.status
        if (status !is ContextMenuState.Status.Open) {
            return
        }
        val contextItems = items()
        val menuItems = remember(contextItems) {
            buildMenu {
                contextItems.map {
                    item(title = it.label.asStringSource(), onClick = {
                        it.onClick()
                    })
                }
            }
        }
        val onCloseRequest = { state.status = ContextMenuState.Status.Closed }
        Popup(
            properties = PopupProperties(
                focusable = true,
            ),
            onDismissRequest = onCloseRequest,
            popupPositionProvider = rememberPopupPositionProviderAtPosition(
                positionPx = status.rect.center
            ),
        ) {
            SubMenu(menuItems, onCloseRequest)
        }
    }

}

@Composable
private fun myContextMenuRepresentation(): ContextMenuRepresentation {
    return remember {
        MyContextMenuRepresentation()
    }
}

@Composable
private fun myDefaultScrollBarStyle(): ScrollbarStyle {
    return ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 12.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = myColors.onBackground / 10,
        hoverColor = myColors.onBackground / 30
    )
}
