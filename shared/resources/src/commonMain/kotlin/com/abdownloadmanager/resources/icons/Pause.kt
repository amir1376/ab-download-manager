package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Pause: ImageVector
    get() {
        if (_Pause != null) {
            return _Pause!!
        }
        _Pause = ImageVector.Builder(
            name = "Pause",
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
                moveTo(6f, 6f)
                curveTo(6f, 5.735f, 6.105f, 5.48f, 6.293f, 5.293f)
                curveTo(6.48f, 5.105f, 6.735f, 5f, 7f, 5f)
                horizontalLineTo(9f)
                curveTo(9.265f, 5f, 9.52f, 5.105f, 9.707f, 5.293f)
                curveTo(9.895f, 5.48f, 10f, 5.735f, 10f, 6f)
                verticalLineTo(18f)
                curveTo(10f, 18.265f, 9.895f, 18.52f, 9.707f, 18.707f)
                curveTo(9.52f, 18.895f, 9.265f, 19f, 9f, 19f)
                horizontalLineTo(7f)
                curveTo(6.735f, 19f, 6.48f, 18.895f, 6.293f, 18.707f)
                curveTo(6.105f, 18.52f, 6f, 18.265f, 6f, 18f)
                verticalLineTo(6f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 6f)
                curveTo(14f, 5.735f, 14.105f, 5.48f, 14.293f, 5.293f)
                curveTo(14.48f, 5.105f, 14.735f, 5f, 15f, 5f)
                horizontalLineTo(17f)
                curveTo(17.265f, 5f, 17.52f, 5.105f, 17.707f, 5.293f)
                curveTo(17.895f, 5.48f, 18f, 5.735f, 18f, 6f)
                verticalLineTo(18f)
                curveTo(18f, 18.265f, 17.895f, 18.52f, 17.707f, 18.707f)
                curveTo(17.52f, 18.895f, 17.265f, 19f, 17f, 19f)
                horizontalLineTo(15f)
                curveTo(14.735f, 19f, 14.48f, 18.895f, 14.293f, 18.707f)
                curveTo(14.105f, 18.52f, 14f, 18.265f, 14f, 18f)
                verticalLineTo(6f)
                close()
            }
        }.build()

        return _Pause!!
    }

@Suppress("ObjectPropertyName")
private var _Pause: ImageVector? = null
