package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.WindowMinimize: ImageVector
    get() {
        if (_WindowMinimize != null) {
            return _WindowMinimize!!
        }
        _WindowMinimize = ImageVector.Builder(
            name = "Default.WindowMinimize",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
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
