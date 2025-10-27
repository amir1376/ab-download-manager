package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.WindowMinimize: ImageVector
    get() {
        if (_WindowMinimize != null) {
            return _WindowMinimize!!
        }
        _WindowMinimize = ImageVector.Builder(
            name = "WindowMinimize",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(0f, 10.909f)
                horizontalLineTo(24f)
                verticalLineTo(13.091f)
                horizontalLineTo(0f)
                verticalLineTo(10.909f)
                close()
            }
        }.build()

        return _WindowMinimize!!
    }

@Suppress("ObjectPropertyName")
private var _WindowMinimize: ImageVector? = null
