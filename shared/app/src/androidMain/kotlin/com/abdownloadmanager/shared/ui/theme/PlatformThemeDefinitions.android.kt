package com.abdownloadmanager.shared.ui.theme

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
        // no providers yet,
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


private val androidTextSizes = TextSizes(
    xs = 10.sp,
    sm = 12.sp,
    base = 14.sp,
    lg = 16.sp,
    xl = 18.sp,
    x2l = 20.sp,
    x3l = 22.sp,
    x4l = 24.sp,
    x5l = 26.sp,
)

private val androidShapes = MyShapes(
    defaultRounded = RoundedCornerShape(12.dp),
)

@Composable
actual fun myPlatformTextSizes(): TextSizes {
    return androidTextSizes
}

@Composable
actual fun myPlatformShapes(): MyShapes {
    return androidShapes
}
private val androidSpacings = MySpacings(
    thumbSize = 48.dp,
    iconSize = 24.dp,
    smallSpace = 4.dp,
    mediumSpace = 8.dp,
    largeSpace = 16.dp,
)

@Composable
actual fun myPlatformSpacing(): MySpacings {
    return androidSpacings
}
