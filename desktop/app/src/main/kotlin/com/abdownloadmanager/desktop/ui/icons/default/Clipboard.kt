package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Clipboard: ImageVector
    get() {
        if (_Clipboard != null) {
            return _Clipboard!!
        }
        _Clipboard = ImageVector.Builder(
            name = "Default.Clipboard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 5f)
                horizontalLineTo(7f)
                curveTo(6.47f, 5f, 5.961f, 5.211f, 5.586f, 5.586f)
                curveTo(5.211f, 5.961f, 5f, 6.47f, 5f, 7f)
                verticalLineTo(19f)
                curveTo(5f, 19.53f, 5.211f, 20.039f, 5.586f, 20.414f)
                curveTo(5.961f, 20.789f, 6.47f, 21f, 7f, 21f)
                horizontalLineTo(17f)
                curveTo(17.53f, 21f, 18.039f, 20.789f, 18.414f, 20.414f)
                curveTo(18.789f, 20.039f, 19f, 19.53f, 19f, 19f)
                verticalLineTo(7f)
                curveTo(19f, 6.47f, 18.789f, 5.961f, 18.414f, 5.586f)
                curveTo(18.039f, 5.211f, 17.53f, 5f, 17f, 5f)
                horizontalLineTo(15f)
                moveTo(9f, 5f)
                curveTo(9f, 4.47f, 9.211f, 3.961f, 9.586f, 3.586f)
                curveTo(9.961f, 3.211f, 10.47f, 3f, 11f, 3f)
                horizontalLineTo(13f)
                curveTo(13.53f, 3f, 14.039f, 3.211f, 14.414f, 3.586f)
                curveTo(14.789f, 3.961f, 15f, 4.47f, 15f, 5f)
                moveTo(9f, 5f)
                curveTo(9f, 5.53f, 9.211f, 6.039f, 9.586f, 6.414f)
                curveTo(9.961f, 6.789f, 10.47f, 7f, 11f, 7f)
                horizontalLineTo(13f)
                curveTo(13.53f, 7f, 14.039f, 6.789f, 14.414f, 6.414f)
                curveTo(14.789f, 6.039f, 15f, 5.53f, 15f, 5f)
            }
        }.build()

        return _Clipboard!!
    }

@Suppress("ObjectPropertyName")
private var _Clipboard: ImageVector? = null
