package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Queue: ImageVector
    get() {
        if (_Queue != null) {
            return _Queue!!
        }
        _Queue = ImageVector.Builder(
            name = "Queue",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 6f)
                horizontalLineTo(2f)
                verticalLineTo(20f)
                curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
                horizontalLineTo(18f)
                verticalLineTo(20f)
                horizontalLineTo(4f)
                verticalLineTo(6f)
                close()
                moveTo(20f, 2f)
                horizontalLineTo(8f)
                curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
                verticalLineTo(16f)
                curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
                horizontalLineTo(20f)
                curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
                verticalLineTo(4f)
                curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
                close()
                moveTo(20f, 16f)
                horizontalLineTo(8f)
                verticalLineTo(4f)
                horizontalLineTo(20f)
                verticalLineTo(16f)
                close()
                moveTo(13f, 15f)
                horizontalLineTo(15f)
                verticalLineTo(11f)
                horizontalLineTo(19f)
                verticalLineTo(9f)
                horizontalLineTo(15f)
                verticalLineTo(5f)
                horizontalLineTo(13f)
                verticalLineTo(9f)
                horizontalLineTo(9f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(15f)
                close()
            }
        }.build()

        return _Queue!!
    }

@Suppress("ObjectPropertyName")
private var _Queue: ImageVector? = null
