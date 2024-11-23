package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Clear: ImageVector
    get() {
        if (_Clear != null) {
            return _Clear!!
        }
        _Clear = ImageVector.Builder(
            name = "Default.Clear",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(5.293f, 5.293f)
                curveTo(5.683f, 4.902f, 6.317f, 4.902f, 6.707f, 5.293f)
                lineTo(12f, 10.586f)
                lineTo(17.293f, 5.293f)
                curveTo(17.683f, 4.902f, 18.317f, 4.902f, 18.707f, 5.293f)
                curveTo(19.098f, 5.683f, 19.098f, 6.317f, 18.707f, 6.707f)
                lineTo(13.414f, 12f)
                lineTo(18.707f, 17.293f)
                curveTo(19.098f, 17.683f, 19.098f, 18.317f, 18.707f, 18.707f)
                curveTo(18.317f, 19.098f, 17.683f, 19.098f, 17.293f, 18.707f)
                lineTo(12f, 13.414f)
                lineTo(6.707f, 18.707f)
                curveTo(6.317f, 19.098f, 5.683f, 19.098f, 5.293f, 18.707f)
                curveTo(4.902f, 18.317f, 4.902f, 17.683f, 5.293f, 17.293f)
                lineTo(10.586f, 12f)
                lineTo(5.293f, 6.707f)
                curveTo(4.902f, 6.317f, 4.902f, 5.683f, 5.293f, 5.293f)
                close()
            }
        }.build()

        return _Clear!!
    }

@Suppress("ObjectPropertyName")
private var _Clear: ImageVector? = null
