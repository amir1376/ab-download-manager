package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Next: ImageVector
    get() {
        if (_Next != null) {
            return _Next!!
        }
        _Next = ImageVector.Builder(
            name = "Next",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.293f, 5.293f)
                curveTo(8.683f, 4.902f, 9.317f, 4.902f, 9.707f, 5.293f)
                lineTo(15.707f, 11.293f)
                curveTo(16.098f, 11.683f, 16.098f, 12.317f, 15.707f, 12.707f)
                lineTo(9.707f, 18.707f)
                curveTo(9.317f, 19.098f, 8.683f, 19.098f, 8.293f, 18.707f)
                curveTo(7.902f, 18.317f, 7.902f, 17.683f, 8.293f, 17.293f)
                lineTo(13.586f, 12f)
                lineTo(8.293f, 6.707f)
                curveTo(7.902f, 6.317f, 7.902f, 5.683f, 8.293f, 5.293f)
                close()
            }
        }.build()

        return _Next!!
    }

@Suppress("ObjectPropertyName")
private var _Next: ImageVector? = null
