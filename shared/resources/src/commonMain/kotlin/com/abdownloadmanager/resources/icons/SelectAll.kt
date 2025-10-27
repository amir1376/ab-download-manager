package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.SelectAll: ImageVector
    get() {
        if (_SelectAll != null) {
            return _SelectAll!!
        }
        _SelectAll = ImageVector.Builder(
            name = "SelectAll",
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
                moveTo(4f, 12f)
                horizontalLineTo(20f)
                moveTo(12f, 4f)
                verticalLineTo(20f)
                moveTo(4f, 6f)
                curveTo(4f, 5.47f, 4.211f, 4.961f, 4.586f, 4.586f)
                curveTo(4.961f, 4.211f, 5.47f, 4f, 6f, 4f)
                horizontalLineTo(18f)
                curveTo(18.53f, 4f, 19.039f, 4.211f, 19.414f, 4.586f)
                curveTo(19.789f, 4.961f, 20f, 5.47f, 20f, 6f)
                verticalLineTo(18f)
                curveTo(20f, 18.53f, 19.789f, 19.039f, 19.414f, 19.414f)
                curveTo(19.039f, 19.789f, 18.53f, 20f, 18f, 20f)
                horizontalLineTo(6f)
                curveTo(5.47f, 20f, 4.961f, 19.789f, 4.586f, 19.414f)
                curveTo(4.211f, 19.039f, 4f, 18.53f, 4f, 18f)
                verticalLineTo(6f)
                close()
            }
        }.build()

        return _SelectAll!!
    }

@Suppress("ObjectPropertyName")
private var _SelectAll: ImageVector? = null
