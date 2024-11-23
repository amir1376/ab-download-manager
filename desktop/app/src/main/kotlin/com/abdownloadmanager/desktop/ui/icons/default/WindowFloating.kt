package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.WindowFloating: ImageVector
    get() {
        if (_WindowFloating != null) {
            return _WindowFloating!!
        }
        _WindowFloating = ImageVector.Builder(
            name = "Default.WindowFloating",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6.545f, 0f)
                verticalLineTo(6.545f)
                horizontalLineTo(0f)
                verticalLineTo(24f)
                horizontalLineTo(17.455f)
                verticalLineTo(17.455f)
                horizontalLineTo(24f)
                verticalLineTo(0f)
                horizontalLineTo(6.545f)
                close()
                moveTo(21.818f, 2.182f)
                horizontalLineTo(8.727f)
                verticalLineTo(6.545f)
                horizontalLineTo(17.455f)
                verticalLineTo(15.273f)
                horizontalLineTo(21.818f)
                verticalLineTo(2.182f)
                close()
                moveTo(15.273f, 15.273f)
                verticalLineTo(21.818f)
                horizontalLineTo(2.182f)
                verticalLineTo(8.727f)
                horizontalLineTo(8.727f)
                horizontalLineTo(15.273f)
                verticalLineTo(15.273f)
                close()
            }
        }.build()

        return _WindowFloating!!
    }

@Suppress("ObjectPropertyName")
private var _WindowFloating: ImageVector? = null
