package org.jetbrains.jewel.window

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jetbrains.JBR
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.window.styling.TitleBarStyle

@Composable
internal fun DecoratedDialogScope.DialogTitleBarOnMacOs(
    modifier: Modifier = Modifier,
    gradientStartColor: Color = Color.Unspecified,
    style: TitleBarStyle = JewelTheme.defaultTitleBarStyle,
    content: @Composable TitleBarScope.(DecoratedDialogState) -> Unit,
) {
    val titleBar = remember { JBR.getWindowDecorations().createCustomTitleBar() }

    DialogTitleBarImpl(
        modifier = modifier.customTitleBarMouseEventHandler(titleBar),
        gradientStartColor = gradientStartColor,
        style = style,
        applyTitleBar = { height, _ ->
            titleBar.height = height.value
            JBR.getWindowDecorations().setCustomTitleBar(window, titleBar)
            PaddingValues(start = titleBar.leftInset.dp, end = titleBar.rightInset.dp)
        },
        content = content,
    )
}
