package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.FileMusic: ImageVector
    get() {
        if (_FileMusic != null) {
            return _FileMusic!!
        }
        _FileMusic = ImageVector.Builder(
            name = "FileMusic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 0.75f,
                strokeAlpha = 0.75f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.25f, 4f)
                curveTo(8.25f, 3.586f, 8.586f, 3.25f, 9f, 3.25f)
                horizontalLineTo(19f)
                curveTo(19.414f, 3.25f, 19.75f, 3.586f, 19.75f, 4f)
                verticalLineTo(17f)
                curveTo(19.75f, 17.995f, 19.355f, 18.948f, 18.652f, 19.652f)
                curveTo(17.948f, 20.355f, 16.995f, 20.75f, 16f, 20.75f)
                curveTo(15.005f, 20.75f, 14.052f, 20.355f, 13.348f, 19.652f)
                curveTo(12.645f, 18.948f, 12.25f, 17.995f, 12.25f, 17f)
                curveTo(12.25f, 16.005f, 12.645f, 15.052f, 13.348f, 14.348f)
                curveTo(14.052f, 13.645f, 15.005f, 13.25f, 16f, 13.25f)
                curveTo(16.816f, 13.25f, 17.605f, 13.516f, 18.25f, 14f)
                verticalLineTo(8.75f)
                horizontalLineTo(9.75f)
                verticalLineTo(17f)
                curveTo(9.75f, 17.995f, 9.355f, 18.948f, 8.652f, 19.652f)
                curveTo(7.948f, 20.355f, 6.995f, 20.75f, 6f, 20.75f)
                curveTo(5.005f, 20.75f, 4.052f, 20.355f, 3.348f, 19.652f)
                curveTo(2.645f, 18.948f, 2.25f, 17.995f, 2.25f, 17f)
                curveTo(2.25f, 16.005f, 2.645f, 15.052f, 3.348f, 14.348f)
                curveTo(4.052f, 13.645f, 5.005f, 13.25f, 6f, 13.25f)
                curveTo(6.816f, 13.25f, 7.605f, 13.516f, 8.25f, 14f)
                verticalLineTo(4f)
                close()
                moveTo(9.75f, 7.25f)
                horizontalLineTo(18.25f)
                verticalLineTo(4.75f)
                horizontalLineTo(9.75f)
                verticalLineTo(7.25f)
                close()
                moveTo(8.25f, 17f)
                curveTo(8.25f, 16.403f, 8.013f, 15.831f, 7.591f, 15.409f)
                curveTo(7.169f, 14.987f, 6.597f, 14.75f, 6f, 14.75f)
                curveTo(5.403f, 14.75f, 4.831f, 14.987f, 4.409f, 15.409f)
                curveTo(3.987f, 15.831f, 3.75f, 16.403f, 3.75f, 17f)
                curveTo(3.75f, 17.597f, 3.987f, 18.169f, 4.409f, 18.591f)
                curveTo(4.831f, 19.013f, 5.403f, 19.25f, 6f, 19.25f)
                curveTo(6.597f, 19.25f, 7.169f, 19.013f, 7.591f, 18.591f)
                curveTo(8.013f, 18.169f, 8.25f, 17.597f, 8.25f, 17f)
                close()
                moveTo(18.25f, 17f)
                curveTo(18.25f, 16.403f, 18.013f, 15.831f, 17.591f, 15.409f)
                curveTo(17.169f, 14.987f, 16.597f, 14.75f, 16f, 14.75f)
                curveTo(15.403f, 14.75f, 14.831f, 14.987f, 14.409f, 15.409f)
                curveTo(13.987f, 15.831f, 13.75f, 16.403f, 13.75f, 17f)
                curveTo(13.75f, 17.597f, 13.987f, 18.169f, 14.409f, 18.591f)
                curveTo(14.831f, 19.013f, 15.403f, 19.25f, 16f, 19.25f)
                curveTo(16.597f, 19.25f, 17.169f, 19.013f, 17.591f, 18.591f)
                curveTo(18.013f, 18.169f, 18.25f, 17.597f, 18.25f, 17f)
                close()
            }
        }.build()

        return _FileMusic!!
    }

@Suppress("ObjectPropertyName")
private var _FileMusic: ImageVector? = null
