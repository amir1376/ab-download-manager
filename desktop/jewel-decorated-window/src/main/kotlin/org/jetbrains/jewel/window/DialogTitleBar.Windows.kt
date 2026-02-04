package org.jetbrains.jewel.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jetbrains.JBR
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.util.isDark
import org.jetbrains.jewel.window.styling.TitleBarStyle

@Composable
internal fun DecoratedDialogScope.DialogTitleBarOnWindows(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    content: @Composable TitleBarScope.(DecoratedDialogState) -> Unit,
) {
    val titleBar = remember { JBR.getWindowDecorations().createCustomTitleBar() }
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    DialogTitleBarImpl(
        modifier = modifier,
        gradientStartColor = gradientStartColor,
        style = style,
        applyTitleBar = { height, _ ->
            titleBar.putProperty("controls.rtl", isRtl)
            titleBar.height = height.value
            titleBar.putProperty("controls.dark", style.colors.background.isDark())
            JBR.getWindowDecorations().setCustomTitleBar(window, titleBar)
            if (isRtl) {
                PaddingValues(start = titleBar.rightInset.dp, end = titleBar.leftInset.dp)
            } else {
                PaddingValues(start = titleBar.leftInset.dp, end = titleBar.rightInset.dp)
            }
        },
        backgroundContent = { Spacer(modifier = modifier.fillMaxSize().customTitleBarMouseEventHandler(titleBar)) },
        content = content,
    )
}
