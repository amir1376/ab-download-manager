package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.WindowClose: ImageVector
    get() {
        if (_WindowClose != null) {
            return _WindowClose!!
        }
        _WindowClose = ImageVector.Builder(
            name = "WindowClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(0f, 22f)
                lineTo(22f, 0f)
                lineTo(24f, 2f)
                lineTo(2f, 24f)
                lineTo(0f, 22f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(22f, 24f)
                lineTo(0f, 2f)
                lineTo(2f, 0f)
                lineTo(24f, 22f)
                lineTo(22f, 24f)
                close()
            }
        }.build()

        return _WindowClose!!
    }

@Suppress("ObjectPropertyName")
private var _WindowClose: ImageVector? = null
