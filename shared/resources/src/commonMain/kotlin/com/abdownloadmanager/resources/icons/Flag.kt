package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Flag: ImageVector
    get() {
        if (_Flag != null) {
            return _Flag!!
        }
        _Flag = ImageVector.Builder(
            name = "Flag",
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
                moveTo(5f, 14f)
                curveTo(5.935f, 13.084f, 7.191f, 12.571f, 8.5f, 12.571f)
                curveTo(9.809f, 12.571f, 11.065f, 13.084f, 12f, 14f)
                curveTo(12.935f, 14.916f, 14.191f, 15.429f, 15.5f, 15.429f)
                curveTo(16.809f, 15.429f, 18.065f, 14.916f, 19f, 14f)
                verticalLineTo(5f)
                curveTo(18.065f, 5.916f, 16.809f, 6.429f, 15.5f, 6.429f)
                curveTo(14.191f, 6.429f, 12.935f, 5.916f, 12f, 5f)
                curveTo(11.065f, 4.084f, 9.809f, 3.571f, 8.5f, 3.571f)
                curveTo(7.191f, 3.571f, 5.935f, 4.084f, 5f, 5f)
                verticalLineTo(14f)
                close()
                moveTo(5f, 14f)
                verticalLineTo(21f)
            }
        }.build()

        return _Flag!!
    }

@Suppress("ObjectPropertyName")
private var _Flag: ImageVector? = null
