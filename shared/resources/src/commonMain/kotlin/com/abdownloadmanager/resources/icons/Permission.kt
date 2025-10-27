package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Permission: ImageVector
    get() {
        if (_Permission != null) {
            return _Permission!!
        }
        _Permission = ImageVector.Builder(
            name = "Permission",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15f, 21f)
                horizontalLineTo(6f)
                curveTo(5.204f, 21f, 4.441f, 20.684f, 3.879f, 20.121f)
                curveTo(3.316f, 19.559f, 3f, 18.796f, 3f, 18f)
                verticalLineTo(17f)
                horizontalLineTo(13f)
                verticalLineTo(19f)
                curveTo(13f, 19.53f, 13.211f, 20.039f, 13.586f, 20.414f)
                curveTo(13.961f, 20.789f, 14.47f, 21f, 15f, 21f)
                close()
                moveTo(15f, 21f)
                curveTo(15.53f, 21f, 16.039f, 20.789f, 16.414f, 20.414f)
                curveTo(16.789f, 20.039f, 17f, 19.53f, 17f, 19f)
                verticalLineTo(5f)
                curveTo(17f, 4.604f, 17.117f, 4.218f, 17.337f, 3.889f)
                curveTo(17.557f, 3.56f, 17.869f, 3.304f, 18.235f, 3.152f)
                curveTo(18.6f, 3.001f, 19.002f, 2.961f, 19.39f, 3.038f)
                curveTo(19.778f, 3.116f, 20.135f, 3.306f, 20.414f, 3.586f)
                curveTo(20.694f, 3.865f, 20.884f, 4.222f, 20.962f, 4.61f)
                curveTo(21.039f, 4.998f, 20.999f, 5.4f, 20.848f, 5.765f)
                curveTo(20.696f, 6.131f, 20.44f, 6.443f, 20.111f, 6.663f)
                curveTo(19.782f, 6.883f, 19.396f, 7f, 19f, 7f)
                horizontalLineTo(17f)
                moveTo(19f, 3f)
                horizontalLineTo(8f)
                curveTo(7.204f, 3f, 6.441f, 3.316f, 5.879f, 3.879f)
                curveTo(5.316f, 4.441f, 5f, 5.204f, 5f, 6f)
                verticalLineTo(17f)
                moveTo(9f, 7f)
                horizontalLineTo(13f)
                moveTo(9f, 11f)
                horizontalLineTo(13f)
            }
        }.build()

        return _Permission!!
    }

@Suppress("ObjectPropertyName")
private var _Permission: ImageVector? = null
