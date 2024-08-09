package com.abdownloadmanager.desktop.ui.theme

import com.abdownloadmanager.desktop.ui.LocalContentAlpha
import com.abdownloadmanager.desktop.ui.LocalContentColor
import com.abdownloadmanager.desktop.ui.LocalTextStyle
import com.abdownloadmanager.desktop.ui.widget.menu.SubMenu
import com.abdownloadmanager.desktop.utils.action.buildMenu
import com.abdownloadmanager.desktop.utils.darker
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.RippleTheme.Companion.defaultRippleAlpha
import androidx.compose.material.ripple.RippleTheme.Companion.defaultRippleColor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberPopupPositionProviderAtPosition
import com.abdownloadmanager.desktop.pages.settings.ThemeManager

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


val darkColors = MyColors(
    //used
    primary = Color(0xFF4791BF),
    onPrimary = Color.White,
    secondary = Color(0xFFB85DFF),
    onSecondary = Color.White,
    background = Color(0xFF16161E),
    onBackground = Color(0xFFAAAAAA),
    onSurface = Color(0xFFAAAAAA),
//        surface = Color(0xff191922),
    surface = Color(0xFF22222A),
    error = Color(0xffff5757),
    onError = Color.White,
    success = Color(0xff14a600),
    onSuccess = Color.White,
    warning = Color(0xFFffbe56),
    onWarning = Color.White,
    info = Color(0xFF2f77d4),
    onInfo = Color.White,
    isLight = false,
    name = "Dark",
    id = "dark",
)
val lightColors = MyColors(
    primary = Color(0xFF4791BF),
    primaryVariant = Color(0xFFAFCEFF).darker(0.5f),
    onPrimary = Color.Black,
    secondary = Color(0xFFB85DFF),
    onSecondary = Color.White,
//        primary = Color(0xFF3B82F6),
    background = Color.White.darker(0.1f),
    backgroundVariant = Color(0xFFfafafa),
    onBackground = Color(0xFF353535),
    onSurface = Color(0xFF353535),
//        surface = Color(0xff191922),
    surface = Color.White.darker(0.15f),
    error = Color(0xffff5757),
    onError = Color.White,
    success = Color(0xff14a600),
    onSuccess = Color.White,
    warning = Color(0xFFffbe56),
    onWarning = Color.White,
    info = Color(0xFF2f77d4),
    onInfo = Color.White,
    isLight = true,
    name = "Light",
    id = "light",
)

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
//    uiScale: Float? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMyColors provides AnimatedColors(myColors, tween(500)),
//        LocalUiScale provides uiScale,
    ) {
        CompositionLocalProvider(
            LocalContextMenuRepresentation provides myContextMenuRepresentation(),
            LocalScrollbarStyle provides myDefaultScrollBarStyle(),
            // there is a modification in newer version of compose that change line height
            // I want to remove material design for good but for now I override this
            LocalRippleTheme provides remember { MyRippleTheme() },
            LocalIndication provides rememberRipple(),
            LocalContentColor provides myColors.onBackground,
            LocalContentAlpha provides 1f,
            LocalTextSizes provides textSizes,
            LocalTextStyle provides LocalTextStyle.current.copy(
                lineHeight = TextUnit.Unspecified,
                fontSize = textSizes.base,
            ),
        ) {
            content()
        }
    }
}

private class MyRippleTheme:RippleTheme{
    @Composable
    override fun defaultColor():Color {
        return defaultRippleColor(
            contentColor = LocalContentColor.current,
            lightTheme = myColors.isLight
        )
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return defaultRippleAlpha(
            contentColor = LocalContentColor.current,
            lightTheme = myColors.isLight
        )
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
                    item(title = it.label, onClick = {
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
        unhoverColor = myColors.onBackground/10,
        hoverColor = myColors.onBackground/30
    )
}
