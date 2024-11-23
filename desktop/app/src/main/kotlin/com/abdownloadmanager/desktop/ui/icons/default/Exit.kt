package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Exit: ImageVector
    get() {
        if (_Exit != null) {
            return _Exit!!
        }
        _Exit = ImageVector.Builder(
            name = "Default.Exit",
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
                moveTo(14f, 8f)
                verticalLineTo(6f)
                curveTo(14f, 5.47f, 13.789f, 4.961f, 13.414f, 4.586f)
                curveTo(13.039f, 4.211f, 12.53f, 4f, 12f, 4f)
                horizontalLineTo(5f)
                curveTo(4.47f, 4f, 3.961f, 4.211f, 3.586f, 4.586f)
                curveTo(3.211f, 4.961f, 3f, 5.47f, 3f, 6f)
                verticalLineTo(18f)
                curveTo(3f, 18.53f, 3.211f, 19.039f, 3.586f, 19.414f)
                curveTo(3.961f, 19.789f, 4.47f, 20f, 5f, 20f)
                horizontalLineTo(12f)
                curveTo(12.53f, 20f, 13.039f, 19.789f, 13.414f, 19.414f)
                curveTo(13.789f, 19.039f, 14f, 18.53f, 14f, 18f)
                verticalLineTo(16f)
                moveTo(9f, 12f)
                horizontalLineTo(21f)
                moveTo(21f, 12f)
                lineTo(18f, 9f)
                moveTo(21f, 12f)
                lineTo(18f, 15f)
            }
        }.build()

        return _Exit!!
    }

@Suppress("ObjectPropertyName")
private var _Exit: ImageVector? = null
