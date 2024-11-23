package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.WindowMaximize: ImageVector
    get() {
        if (_WindowMaximize != null) {
            return _WindowMaximize!!
        }
        _WindowMaximize = ImageVector.Builder(
            name = "Default.WindowMaximize",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(21.818f, 2.182f)
                horizontalLineTo(2.182f)
                verticalLineTo(21.818f)
                horizontalLineTo(21.818f)
                verticalLineTo(2.182f)
                close()
                moveTo(0f, 0f)
                verticalLineTo(24f)
                horizontalLineTo(24f)
                verticalLineTo(0f)
                horizontalLineTo(0f)
                close()
            }
        }.build()

        return _WindowMaximize!!
    }

@Suppress("ObjectPropertyName")
private var _WindowMaximize: ImageVector? = null
