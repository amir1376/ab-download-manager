package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Minus: ImageVector
    get() {
        if (_Minus != null) {
            return _Minus!!
        }
        _Minus = ImageVector.Builder(
            name = "Minus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19f, 11f)
                curveTo(19.552f, 11f, 20f, 11.448f, 20f, 12f)
                curveTo(20f, 12.552f, 19.552f, 13f, 19f, 13f)
                horizontalLineTo(5f)
                curveTo(4.448f, 13f, 4f, 12.552f, 4f, 12f)
                curveTo(4f, 11.448f, 4.448f, 11f, 5f, 11f)
                horizontalLineTo(19f)
                close()
            }
        }.build()

        return _Minus!!
    }

@Suppress("ObjectPropertyName")
private var _Minus: ImageVector? = null
