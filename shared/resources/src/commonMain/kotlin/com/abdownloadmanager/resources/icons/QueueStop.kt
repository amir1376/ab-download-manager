package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.QueueStop: ImageVector
    get() {
        if (_QueueStop != null) {
            return _QueueStop!!
        }
        _QueueStop = ImageVector.Builder(
            name = "QueueStop",
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
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(13.952f, 13.064f)
                curveTo(13.716f, 13.064f, 13.491f, 13.158f, 13.324f, 13.324f)
                curveTo(13.158f, 13.491f, 13.064f, 13.716f, 13.064f, 13.952f)
                verticalLineTo(21.048f)
                curveTo(13.064f, 21.284f, 13.158f, 21.509f, 13.324f, 21.676f)
                curveTo(13.491f, 21.842f, 13.716f, 21.935f, 13.952f, 21.935f)
                horizontalLineTo(21.048f)
                curveTo(21.284f, 21.935f, 21.509f, 21.842f, 21.676f, 21.676f)
                curveTo(21.842f, 21.509f, 21.935f, 21.284f, 21.935f, 21.048f)
                verticalLineTo(13.952f)
                curveTo(21.935f, 13.716f, 21.842f, 13.491f, 21.676f, 13.324f)
                curveTo(21.509f, 13.158f, 21.284f, 13.064f, 21.048f, 13.064f)
                horizontalLineTo(13.952f)
                close()
                moveTo(12.572f, 12.572f)
                curveTo(12.938f, 12.206f, 13.434f, 12f, 13.952f, 12f)
                horizontalLineTo(21.048f)
                curveTo(21.566f, 12f, 22.062f, 12.206f, 22.428f, 12.572f)
                curveTo(22.794f, 12.938f, 23f, 13.434f, 23f, 13.952f)
                verticalLineTo(21.048f)
                curveTo(23f, 21.566f, 22.794f, 22.062f, 22.428f, 22.428f)
                curveTo(22.062f, 22.794f, 21.566f, 23f, 21.048f, 23f)
                horizontalLineTo(13.952f)
                curveTo(13.434f, 23f, 12.938f, 22.794f, 12.572f, 22.428f)
                curveTo(12.206f, 22.062f, 12f, 21.566f, 12f, 21.048f)
                verticalLineTo(13.952f)
                curveTo(12f, 13.434f, 12.206f, 12.938f, 12.572f, 12.572f)
                close()
            }
        }.build()

        return _QueueStop!!
    }

@Suppress("ObjectPropertyName")
private var _QueueStop: ImageVector? = null
