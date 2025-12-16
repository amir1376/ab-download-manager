package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Delete: ImageVector
    get() {
        if (_Delete != null) {
            return _Delete!!
        }
        _Delete = ImageVector.Builder(
            name = "Delete",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10f, 3.75f)
                curveTo(9.934f, 3.75f, 9.87f, 3.776f, 9.823f, 3.823f)
                curveTo(9.776f, 3.87f, 9.75f, 3.934f, 9.75f, 4f)
                verticalLineTo(6.25f)
                horizontalLineTo(14.25f)
                verticalLineTo(4f)
                curveTo(14.25f, 3.934f, 14.224f, 3.87f, 14.177f, 3.823f)
                curveTo(14.13f, 3.776f, 14.066f, 3.75f, 14f, 3.75f)
                horizontalLineTo(10f)
                close()
                moveTo(15.75f, 6.25f)
                verticalLineTo(4f)
                curveTo(15.75f, 3.536f, 15.566f, 3.091f, 15.237f, 2.763f)
                curveTo(14.909f, 2.434f, 14.464f, 2.25f, 14f, 2.25f)
                horizontalLineTo(10f)
                curveTo(9.536f, 2.25f, 9.091f, 2.434f, 8.763f, 2.763f)
                curveTo(8.434f, 3.091f, 8.25f, 3.536f, 8.25f, 4f)
                verticalLineTo(6.25f)
                horizontalLineTo(5.009f)
                curveTo(5.003f, 6.25f, 4.998f, 6.25f, 4.993f, 6.25f)
                horizontalLineTo(4f)
                curveTo(3.586f, 6.25f, 3.25f, 6.586f, 3.25f, 7f)
                curveTo(3.25f, 7.414f, 3.586f, 7.75f, 4f, 7.75f)
                horizontalLineTo(4.31f)
                lineTo(5.25f, 19.034f)
                curveTo(5.259f, 19.751f, 5.548f, 20.437f, 6.055f, 20.944f)
                curveTo(6.571f, 21.46f, 7.271f, 21.75f, 8f, 21.75f)
                horizontalLineTo(16f)
                curveTo(16.729f, 21.75f, 17.429f, 21.46f, 17.944f, 20.944f)
                curveTo(18.452f, 20.437f, 18.741f, 19.751f, 18.75f, 19.034f)
                lineTo(19.69f, 7.75f)
                horizontalLineTo(20f)
                curveTo(20.414f, 7.75f, 20.75f, 7.414f, 20.75f, 7f)
                curveTo(20.75f, 6.586f, 20.414f, 6.25f, 20f, 6.25f)
                horizontalLineTo(19.007f)
                curveTo(19.002f, 6.25f, 18.997f, 6.25f, 18.991f, 6.25f)
                horizontalLineTo(15.75f)
                close()
                moveTo(5.815f, 7.75f)
                lineTo(6.747f, 18.938f)
                curveTo(6.749f, 18.958f, 6.75f, 18.979f, 6.75f, 19f)
                curveTo(6.75f, 19.331f, 6.882f, 19.649f, 7.116f, 19.884f)
                curveTo(7.351f, 20.118f, 7.668f, 20.25f, 8f, 20.25f)
                horizontalLineTo(16f)
                curveTo(16.331f, 20.25f, 16.649f, 20.118f, 16.884f, 19.884f)
                curveTo(17.118f, 19.649f, 17.25f, 19.331f, 17.25f, 19f)
                curveTo(17.25f, 18.979f, 17.251f, 18.958f, 17.253f, 18.938f)
                lineTo(18.185f, 7.75f)
                horizontalLineTo(5.815f)
                close()
                moveTo(9.47f, 12.53f)
                curveTo(9.177f, 12.237f, 9.177f, 11.763f, 9.47f, 11.47f)
                curveTo(9.763f, 11.177f, 10.237f, 11.177f, 10.53f, 11.47f)
                lineTo(12f, 12.939f)
                lineTo(13.47f, 11.47f)
                curveTo(13.763f, 11.177f, 14.237f, 11.177f, 14.53f, 11.47f)
                curveTo(14.823f, 11.763f, 14.823f, 12.237f, 14.53f, 12.53f)
                lineTo(13.061f, 14f)
                lineTo(14.53f, 15.47f)
                curveTo(14.823f, 15.763f, 14.823f, 16.237f, 14.53f, 16.53f)
                curveTo(14.237f, 16.823f, 13.763f, 16.823f, 13.47f, 16.53f)
                lineTo(12f, 15.061f)
                lineTo(10.53f, 16.53f)
                curveTo(10.237f, 16.823f, 9.763f, 16.823f, 9.47f, 16.53f)
                curveTo(9.177f, 16.237f, 9.177f, 15.763f, 9.47f, 15.47f)
                lineTo(10.939f, 14f)
                lineTo(9.47f, 12.53f)
                close()
            }
        }.build()

        return _Delete!!
    }

@Suppress("ObjectPropertyName")
private var _Delete: ImageVector? = null
