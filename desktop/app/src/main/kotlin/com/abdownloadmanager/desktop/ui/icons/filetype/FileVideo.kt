package com.abdownloadmanager.desktop.ui.icons.filetype

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.FileType.FileVideo: ImageVector
    get() {
        if (_FileVideo != null) {
            return _FileVideo!!
        }
        _FileVideo = ImageVector.Builder(
            name = "FileType.FileVideo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15f, 10f)
                lineTo(19.553f, 7.724f)
                curveTo(19.705f, 7.648f, 19.875f, 7.612f, 20.045f, 7.62f)
                curveTo(20.215f, 7.627f, 20.381f, 7.678f, 20.526f, 7.768f)
                curveTo(20.671f, 7.857f, 20.79f, 7.982f, 20.873f, 8.131f)
                curveTo(20.956f, 8.28f, 21f, 8.448f, 21f, 8.618f)
                verticalLineTo(15.382f)
                curveTo(21f, 15.552f, 20.956f, 15.72f, 20.873f, 15.869f)
                curveTo(20.79f, 16.017f, 20.671f, 16.143f, 20.526f, 16.232f)
                curveTo(20.381f, 16.322f, 20.215f, 16.373f, 20.045f, 16.381f)
                curveTo(19.875f, 16.388f, 19.705f, 16.352f, 19.553f, 16.276f)
                lineTo(15f, 14f)
                verticalLineTo(10f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 8f)
                curveTo(3f, 7.47f, 3.211f, 6.961f, 3.586f, 6.586f)
                curveTo(3.961f, 6.211f, 4.47f, 6f, 5f, 6f)
                horizontalLineTo(13f)
                curveTo(13.53f, 6f, 14.039f, 6.211f, 14.414f, 6.586f)
                curveTo(14.789f, 6.961f, 15f, 7.47f, 15f, 8f)
                verticalLineTo(16f)
                curveTo(15f, 16.53f, 14.789f, 17.039f, 14.414f, 17.414f)
                curveTo(14.039f, 17.789f, 13.53f, 18f, 13f, 18f)
                horizontalLineTo(5f)
                curveTo(4.47f, 18f, 3.961f, 17.789f, 3.586f, 17.414f)
                curveTo(3.211f, 17.039f, 3f, 16.53f, 3f, 16f)
                verticalLineTo(8f)
                close()
            }
        }.build()

        return _FileVideo!!
    }

@Suppress("ObjectPropertyName")
private var _FileVideo: ImageVector? = null
