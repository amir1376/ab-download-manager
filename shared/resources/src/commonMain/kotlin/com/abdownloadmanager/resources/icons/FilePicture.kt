package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.FilePicture: ImageVector
    get() {
        if (_FilePicture != null) {
            return _FilePicture!!
        }
        _FilePicture = ImageVector.Builder(
            name = "FilePicture",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6f, 3.75f)
                curveTo(5.403f, 3.75f, 4.831f, 3.987f, 4.409f, 4.409f)
                curveTo(3.987f, 4.831f, 3.75f, 5.403f, 3.75f, 6f)
                verticalLineTo(14.189f)
                lineTo(7.47f, 10.47f)
                lineTo(7.48f, 10.46f)
                curveTo(8.057f, 9.905f, 8.753f, 9.58f, 9.5f, 9.58f)
                curveTo(10.247f, 9.58f, 10.943f, 9.905f, 11.52f, 10.46f)
                lineTo(11.53f, 10.47f)
                lineTo(14f, 12.939f)
                lineTo(14.47f, 12.47f)
                lineTo(14.48f, 12.46f)
                curveTo(15.057f, 11.905f, 15.753f, 11.58f, 16.5f, 11.58f)
                curveTo(17.247f, 11.58f, 17.943f, 11.905f, 18.52f, 12.46f)
                lineTo(18.53f, 12.47f)
                lineTo(20.25f, 14.189f)
                verticalLineTo(6f)
                curveTo(20.25f, 5.403f, 20.013f, 4.831f, 19.591f, 4.409f)
                curveTo(19.169f, 3.987f, 18.597f, 3.75f, 18f, 3.75f)
                horizontalLineTo(6f)
                close()
                moveTo(21.75f, 6f)
                curveTo(21.75f, 5.005f, 21.355f, 4.052f, 20.652f, 3.348f)
                curveTo(19.948f, 2.645f, 18.995f, 2.25f, 18f, 2.25f)
                horizontalLineTo(6f)
                curveTo(5.005f, 2.25f, 4.052f, 2.645f, 3.348f, 3.348f)
                curveTo(2.645f, 4.052f, 2.25f, 5.005f, 2.25f, 6f)
                verticalLineTo(18f)
                curveTo(2.25f, 18.995f, 2.645f, 19.948f, 3.348f, 20.652f)
                curveTo(4.052f, 21.355f, 5.005f, 21.75f, 6f, 21.75f)
                horizontalLineTo(18f)
                curveTo(18.995f, 21.75f, 19.948f, 21.355f, 20.652f, 20.652f)
                curveTo(21.355f, 19.948f, 21.75f, 18.995f, 21.75f, 18f)
                verticalLineTo(6f)
                close()
                moveTo(20.25f, 16.311f)
                lineTo(17.475f, 13.536f)
                curveTo(17.125f, 13.201f, 16.788f, 13.08f, 16.5f, 13.08f)
                curveTo(16.212f, 13.08f, 15.875f, 13.201f, 15.525f, 13.536f)
                lineTo(15.061f, 14f)
                lineTo(16.53f, 15.47f)
                curveTo(16.823f, 15.763f, 16.823f, 16.237f, 16.53f, 16.53f)
                curveTo(16.237f, 16.823f, 15.763f, 16.823f, 15.47f, 16.53f)
                lineTo(10.475f, 11.536f)
                curveTo(10.125f, 11.201f, 9.788f, 11.08f, 9.5f, 11.08f)
                curveTo(9.212f, 11.08f, 8.875f, 11.201f, 8.525f, 11.536f)
                lineTo(3.75f, 16.311f)
                verticalLineTo(18f)
                curveTo(3.75f, 18.597f, 3.987f, 19.169f, 4.409f, 19.591f)
                curveTo(4.831f, 20.013f, 5.403f, 20.25f, 6f, 20.25f)
                horizontalLineTo(18f)
                curveTo(18.597f, 20.25f, 19.169f, 20.013f, 19.591f, 19.591f)
                curveTo(20.013f, 19.169f, 20.25f, 18.597f, 20.25f, 18f)
                verticalLineTo(16.311f)
                close()
                moveTo(14.25f, 8f)
                curveTo(14.25f, 7.586f, 14.586f, 7.25f, 15f, 7.25f)
                horizontalLineTo(15.01f)
                curveTo(15.424f, 7.25f, 15.76f, 7.586f, 15.76f, 8f)
                curveTo(15.76f, 8.414f, 15.424f, 8.75f, 15.01f, 8.75f)
                horizontalLineTo(15f)
                curveTo(14.586f, 8.75f, 14.25f, 8.414f, 14.25f, 8f)
                close()
            }
        }.build()

        return _FilePicture!!
    }

@Suppress("ObjectPropertyName")
private var _FilePicture: ImageVector? = null
