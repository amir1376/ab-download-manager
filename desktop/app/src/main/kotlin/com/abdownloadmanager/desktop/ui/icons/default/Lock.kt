package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Lock: ImageVector
    get() {
        if (_Lock != null) {
            return _Lock!!
        }
        _Lock = ImageVector.Builder(
            name = "Default.Lock",
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
                moveTo(8f, 11f)
                verticalLineTo(7f)
                curveTo(8f, 5.939f, 8.421f, 4.922f, 9.172f, 4.172f)
                curveTo(9.922f, 3.421f, 10.939f, 3f, 12f, 3f)
                curveTo(13.061f, 3f, 14.078f, 3.421f, 14.828f, 4.172f)
                curveTo(15.579f, 4.922f, 16f, 5.939f, 16f, 7f)
                verticalLineTo(11f)
                moveTo(5f, 13f)
                curveTo(5f, 12.47f, 5.211f, 11.961f, 5.586f, 11.586f)
                curveTo(5.961f, 11.211f, 6.47f, 11f, 7f, 11f)
                horizontalLineTo(17f)
                curveTo(17.53f, 11f, 18.039f, 11.211f, 18.414f, 11.586f)
                curveTo(18.789f, 11.961f, 19f, 12.47f, 19f, 13f)
                verticalLineTo(19f)
                curveTo(19f, 19.53f, 18.789f, 20.039f, 18.414f, 20.414f)
                curveTo(18.039f, 20.789f, 17.53f, 21f, 17f, 21f)
                horizontalLineTo(7f)
                curveTo(6.47f, 21f, 5.961f, 20.789f, 5.586f, 20.414f)
                curveTo(5.211f, 20.039f, 5f, 19.53f, 5f, 19f)
                verticalLineTo(13f)
                close()
                moveTo(11f, 16f)
                curveTo(11f, 16.265f, 11.105f, 16.52f, 11.293f, 16.707f)
                curveTo(11.48f, 16.895f, 11.735f, 17f, 12f, 17f)
                curveTo(12.265f, 17f, 12.52f, 16.895f, 12.707f, 16.707f)
                curveTo(12.895f, 16.52f, 13f, 16.265f, 13f, 16f)
                curveTo(13f, 15.735f, 12.895f, 15.48f, 12.707f, 15.293f)
                curveTo(12.52f, 15.105f, 12.265f, 15f, 12f, 15f)
                curveTo(11.735f, 15f, 11.48f, 15.105f, 11.293f, 15.293f)
                curveTo(11.105f, 15.48f, 11f, 15.735f, 11f, 16f)
                close()
            }
        }.build()

        return _Lock!!
    }

@Suppress("ObjectPropertyName")
private var _Lock: ImageVector? = null
