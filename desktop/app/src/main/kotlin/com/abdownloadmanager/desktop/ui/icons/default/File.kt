package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.File: ImageVector
    get() {
        if (_File != null) {
            return _File!!
        }
        _File = ImageVector.Builder(
            name = "Default.File",
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
                moveTo(14f, 3f)
                verticalLineTo(7f)
                curveTo(14f, 7.265f, 14.105f, 7.52f, 14.293f, 7.707f)
                curveTo(14.48f, 7.895f, 14.735f, 8f, 15f, 8f)
                horizontalLineTo(19f)
                moveTo(14f, 3f)
                horizontalLineTo(7f)
                curveTo(6.47f, 3f, 5.961f, 3.211f, 5.586f, 3.586f)
                curveTo(5.211f, 3.961f, 5f, 4.47f, 5f, 5f)
                verticalLineTo(19f)
                curveTo(5f, 19.53f, 5.211f, 20.039f, 5.586f, 20.414f)
                curveTo(5.961f, 20.789f, 6.47f, 21f, 7f, 21f)
                horizontalLineTo(17f)
                curveTo(17.53f, 21f, 18.039f, 20.789f, 18.414f, 20.414f)
                curveTo(18.789f, 20.039f, 19f, 19.53f, 19f, 19f)
                verticalLineTo(8f)
                moveTo(14f, 3f)
                lineTo(19f, 8f)
            }
        }.build()

        return _File!!
    }

@Suppress("ObjectPropertyName")
private var _File: ImageVector? = null
