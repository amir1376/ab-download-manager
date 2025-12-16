package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.QueueStart: ImageVector
    get() {
        if (_QueueStart != null) {
            return _QueueStart!!
        }
        _QueueStart = ImageVector.Builder(
            name = "QueueStart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(2f, 6f)
                horizontalLineTo(4f)
                verticalLineTo(20f)
                horizontalLineTo(10f)
                verticalLineTo(22f)
                horizontalLineTo(4f)
                curveTo(2.9f, 22f, 2f, 21.1f, 2f, 20f)
                verticalLineTo(6f)
                close()
                moveTo(22f, 10f)
                verticalLineTo(4f)
                curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
                horizontalLineTo(8f)
                curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
                verticalLineTo(16f)
                curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
                horizontalLineTo(10f)
                verticalLineTo(16f)
                horizontalLineTo(8f)
                verticalLineTo(4f)
                horizontalLineTo(20f)
                verticalLineTo(10f)
                horizontalLineTo(22f)
                close()
                moveTo(19f, 10f)
                verticalLineTo(9f)
                horizontalLineTo(15f)
                verticalLineTo(5f)
                horizontalLineTo(13f)
                verticalLineTo(9f)
                horizontalLineTo(9f)
                verticalLineTo(11f)
                horizontalLineTo(10.764f)
                curveTo(11.313f, 10.386f, 12.111f, 10f, 13f, 10f)
                horizontalLineTo(19f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.2f
            ) {
                moveTo(21.963f, 17.357f)
                lineTo(13.18f, 21.794f)
                curveTo(12.914f, 21.928f, 12.6f, 21.735f, 12.6f, 21.437f)
                verticalLineTo(12.563f)
                curveTo(12.6f, 12.265f, 12.914f, 12.072f, 13.18f, 12.206f)
                lineTo(21.963f, 16.643f)
                curveTo(22.256f, 16.791f, 22.256f, 17.209f, 21.963f, 17.357f)
                close()
            }
        }.build()

        return _QueueStart!!
    }

@Suppress("ObjectPropertyName")
private var _QueueStart: ImageVector? = null
