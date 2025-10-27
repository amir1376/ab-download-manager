package com.abdownloadmanager.shared.ui.theme

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.MyShapes
import com.abdownloadmanager.shared.util.ui.theme.MySpacings
import com.abdownloadmanager.shared.util.ui.theme.TextSizes
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.ThumbStyle
import io.github.oikvpqya.compose.fastscroller.TrackStyle

@Composable
actual fun PlatformDependentProviders(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        LocalContextMenuRepresentation provides myContextMenuRepresentation(),
        content = content,
    )
}

@Composable
actual fun myPlatformScrollbarStyle(): ScrollbarStyle {
    val shape = RoundedCornerShape(4.dp)
    return ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 6.dp,
        thumbStyle = ThumbStyle(
            shape = shape,
            unhoverColor = myColors.onBackground / 10,
            hoverColor = myColors.onBackground / 30,
        ),
        trackStyle = TrackStyle(
            unhoverColor = Color.Transparent,
            hoverColor = Color.Transparent,
            shape = RectangleShape,
        ),
        hoverDurationMillis = 300,
    )
}

private val desktopTextSizes = TextSizes(
    xs = 8.sp,
    sm = 10.sp,
    base = 12.sp,
    lg = 14.sp,
    xl = 16.sp,
    x2l = 18.sp,
    x3l = 20.sp,
    x4l = 22.sp,
    x5l = 24.sp,
)
private val desktopSpacings = MySpacings(
    thumbSize = 24.dp,
    iconSize = 16.dp,
    smallSpace = 4.dp,
    mediumSpace = 8.dp,
    largeSpace = 16.dp,
)

val desktopShapes = MyShapes(
    defaultRounded = RoundedCornerShape(6.dp)
)

@Composable
actual fun myPlatformTextSizes(): TextSizes {
    return desktopTextSizes
}

@Composable
actual fun myPlatformShapes(): MyShapes {
    return desktopShapes
}

@Composable
actual fun myPlatformSpacing(): MySpacings {
    return desktopSpacings
}
