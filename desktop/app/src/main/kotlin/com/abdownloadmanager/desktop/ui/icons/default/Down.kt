package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Down: ImageVector
    get() {
        if (_Down != null) {
            return _Down!!
        }
        _Down = ImageVector.Builder(
            name = "Default.Down",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.293f, 8.293f)
                curveTo(5.683f, 7.902f, 6.317f, 7.902f, 6.707f, 8.293f)
                lineTo(12f, 13.586f)
                lineTo(17.293f, 8.293f)
                curveTo(17.683f, 7.902f, 18.317f, 7.902f, 18.707f, 8.293f)
                curveTo(19.098f, 8.683f, 19.098f, 9.317f, 18.707f, 9.707f)
                lineTo(12.707f, 15.707f)
                curveTo(12.317f, 16.098f, 11.683f, 16.098f, 11.293f, 15.707f)
                lineTo(5.293f, 9.707f)
                curveTo(4.902f, 9.317f, 4.902f, 8.683f, 5.293f, 8.293f)
                close()
            }
        }.build()

        return _Down!!
    }

@Suppress("ObjectPropertyName")
private var _Down: ImageVector? = null
