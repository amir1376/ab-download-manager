package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Copy: ImageVector
    get() {
        if (_Copy != null) {
            return _Copy!!
        }
        _Copy = ImageVector.Builder(
            name = "Copy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16f, 8f)
                verticalLineTo(6f)
                curveTo(16f, 5.47f, 15.789f, 4.961f, 15.414f, 4.586f)
                curveTo(15.039f, 4.211f, 14.53f, 4f, 14f, 4f)
                horizontalLineTo(6f)
                curveTo(5.47f, 4f, 4.961f, 4.211f, 4.586f, 4.586f)
                curveTo(4.211f, 4.961f, 4f, 5.47f, 4f, 6f)
                verticalLineTo(14f)
                curveTo(4f, 14.53f, 4.211f, 15.039f, 4.586f, 15.414f)
                curveTo(4.961f, 15.789f, 5.47f, 16f, 6f, 16f)
                horizontalLineTo(8f)
                moveTo(8f, 10f)
                curveTo(8f, 9.47f, 8.211f, 8.961f, 8.586f, 8.586f)
                curveTo(8.961f, 8.211f, 9.47f, 8f, 10f, 8f)
                horizontalLineTo(18f)
                curveTo(18.53f, 8f, 19.039f, 8.211f, 19.414f, 8.586f)
                curveTo(19.789f, 8.961f, 20f, 9.47f, 20f, 10f)
                verticalLineTo(18f)
                curveTo(20f, 18.53f, 19.789f, 19.039f, 19.414f, 19.414f)
                curveTo(19.039f, 19.789f, 18.53f, 20f, 18f, 20f)
                horizontalLineTo(10f)
                curveTo(9.47f, 20f, 8.961f, 19.789f, 8.586f, 19.414f)
                curveTo(8.211f, 19.039f, 8f, 18.53f, 8f, 18f)
                verticalLineTo(10f)
                close()
            }
        }.build()

        return _Copy!!
    }

@Suppress("ObjectPropertyName")
private var _Copy: ImageVector? = null
