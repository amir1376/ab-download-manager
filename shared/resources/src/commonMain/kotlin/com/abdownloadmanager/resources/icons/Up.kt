package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Up: ImageVector
    get() {
        if (_Up != null) {
            return _Up!!
        }
        _Up = ImageVector.Builder(
            name = "Up",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.293f, 8.293f)
                curveTo(11.683f, 7.902f, 12.317f, 7.902f, 12.707f, 8.293f)
                lineTo(18.707f, 14.293f)
                curveTo(19.098f, 14.683f, 19.098f, 15.317f, 18.707f, 15.707f)
                curveTo(18.317f, 16.098f, 17.683f, 16.098f, 17.293f, 15.707f)
                lineTo(12f, 10.414f)
                lineTo(6.707f, 15.707f)
                curveTo(6.317f, 16.098f, 5.683f, 16.098f, 5.293f, 15.707f)
                curveTo(4.902f, 15.317f, 4.902f, 14.683f, 5.293f, 14.293f)
                lineTo(11.293f, 8.293f)
                close()
            }
        }.build()

        return _Up!!
    }

@Suppress("ObjectPropertyName")
private var _Up: ImageVector? = null
