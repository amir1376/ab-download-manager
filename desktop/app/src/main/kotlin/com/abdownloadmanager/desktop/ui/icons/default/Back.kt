package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Back: ImageVector
    get() {
        if (_Back != null) {
            return _Back!!
        }
        _Back = ImageVector.Builder(
            name = "Default.Back",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(15.707f, 5.293f)
                curveTo(16.098f, 5.683f, 16.098f, 6.317f, 15.707f, 6.707f)
                lineTo(10.414f, 12f)
                lineTo(15.707f, 17.293f)
                curveTo(16.098f, 17.683f, 16.098f, 18.317f, 15.707f, 18.707f)
                curveTo(15.317f, 19.098f, 14.683f, 19.098f, 14.293f, 18.707f)
                lineTo(8.293f, 12.707f)
                curveTo(7.902f, 12.317f, 7.902f, 11.683f, 8.293f, 11.293f)
                lineTo(14.293f, 5.293f)
                curveTo(14.683f, 4.902f, 15.317f, 4.902f, 15.707f, 5.293f)
                close()
            }
        }.build()

        return _Back!!
    }

@Suppress("ObjectPropertyName")
private var _Back: ImageVector? = null
