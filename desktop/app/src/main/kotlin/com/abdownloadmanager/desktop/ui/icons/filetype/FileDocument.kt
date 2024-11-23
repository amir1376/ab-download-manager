package com.abdownloadmanager.desktop.ui.icons.filetype

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.FileType.FileDocument: ImageVector
    get() {
        if (_FileDocument != null) {
            return _FileDocument!!
        }
        _FileDocument = ImageVector.Builder(
            name = "FileType.FileDocument",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(7f, 3.75f)
                curveTo(6.668f, 3.75f, 6.351f, 3.882f, 6.116f, 4.116f)
                curveTo(5.882f, 4.351f, 5.75f, 4.668f, 5.75f, 5f)
                verticalLineTo(19f)
                curveTo(5.75f, 19.331f, 5.882f, 19.649f, 6.116f, 19.884f)
                curveTo(6.351f, 20.118f, 6.668f, 20.25f, 7f, 20.25f)
                horizontalLineTo(17f)
                curveTo(17.331f, 20.25f, 17.649f, 20.118f, 17.884f, 19.884f)
                curveTo(18.118f, 19.649f, 18.25f, 19.331f, 18.25f, 19f)
                verticalLineTo(8.75f)
                horizontalLineTo(15f)
                curveTo(14.536f, 8.75f, 14.091f, 8.566f, 13.763f, 8.237f)
                curveTo(13.434f, 7.909f, 13.25f, 7.464f, 13.25f, 7f)
                verticalLineTo(3.75f)
                horizontalLineTo(7f)
                close()
                moveTo(14.75f, 4.811f)
                lineTo(17.189f, 7.25f)
                horizontalLineTo(15f)
                curveTo(14.934f, 7.25f, 14.87f, 7.224f, 14.823f, 7.177f)
                curveTo(14.776f, 7.13f, 14.75f, 7.066f, 14.75f, 7f)
                verticalLineTo(4.811f)
                close()
                moveTo(5.055f, 3.055f)
                curveTo(5.571f, 2.54f, 6.271f, 2.25f, 7f, 2.25f)
                horizontalLineTo(14f)
                curveTo(14.199f, 2.25f, 14.39f, 2.329f, 14.53f, 2.47f)
                lineTo(19.53f, 7.47f)
                curveTo(19.671f, 7.61f, 19.75f, 7.801f, 19.75f, 8f)
                verticalLineTo(19f)
                curveTo(19.75f, 19.729f, 19.46f, 20.429f, 18.944f, 20.944f)
                curveTo(18.429f, 21.46f, 17.729f, 21.75f, 17f, 21.75f)
                horizontalLineTo(7f)
                curveTo(6.271f, 21.75f, 5.571f, 21.46f, 5.055f, 20.944f)
                curveTo(4.54f, 20.429f, 4.25f, 19.729f, 4.25f, 19f)
                verticalLineTo(5f)
                curveTo(4.25f, 4.271f, 4.54f, 3.571f, 5.055f, 3.055f)
                close()
                moveTo(8.25f, 9f)
                curveTo(8.25f, 8.586f, 8.586f, 8.25f, 9f, 8.25f)
                horizontalLineTo(10f)
                curveTo(10.414f, 8.25f, 10.75f, 8.586f, 10.75f, 9f)
                curveTo(10.75f, 9.414f, 10.414f, 9.75f, 10f, 9.75f)
                horizontalLineTo(9f)
                curveTo(8.586f, 9.75f, 8.25f, 9.414f, 8.25f, 9f)
                close()
                moveTo(8.25f, 13f)
                curveTo(8.25f, 12.586f, 8.586f, 12.25f, 9f, 12.25f)
                horizontalLineTo(15f)
                curveTo(15.414f, 12.25f, 15.75f, 12.586f, 15.75f, 13f)
                curveTo(15.75f, 13.414f, 15.414f, 13.75f, 15f, 13.75f)
                horizontalLineTo(9f)
                curveTo(8.586f, 13.75f, 8.25f, 13.414f, 8.25f, 13f)
                close()
                moveTo(8.25f, 17f)
                curveTo(8.25f, 16.586f, 8.586f, 16.25f, 9f, 16.25f)
                horizontalLineTo(15f)
                curveTo(15.414f, 16.25f, 15.75f, 16.586f, 15.75f, 17f)
                curveTo(15.75f, 17.414f, 15.414f, 17.75f, 15f, 17.75f)
                horizontalLineTo(9f)
                curveTo(8.586f, 17.75f, 8.25f, 17.414f, 8.25f, 17f)
                close()
            }
        }.build()

        return _FileDocument!!
    }

@Suppress("ObjectPropertyName")
private var _FileDocument: ImageVector? = null
