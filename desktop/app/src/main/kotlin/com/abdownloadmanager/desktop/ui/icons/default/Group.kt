package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Group: ImageVector
    get() {
        if (_Group != null) {
            return _Group!!
        }
        _Group = ImageVector.Builder(
            name = "Default.Group",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 18f)
                verticalLineTo(17f)
                curveTo(7f, 15.674f, 7.527f, 14.402f, 8.464f, 13.465f)
                curveTo(9.402f, 12.527f, 10.674f, 12f, 12f, 12f)
                moveTo(12f, 12f)
                curveTo(13.326f, 12f, 14.598f, 12.527f, 15.535f, 13.465f)
                curveTo(16.473f, 14.402f, 17f, 15.674f, 17f, 17f)
                verticalLineTo(18f)
                moveTo(12f, 12f)
                curveTo(12.796f, 12f, 13.559f, 11.684f, 14.121f, 11.121f)
                curveTo(14.684f, 10.559f, 15f, 9.796f, 15f, 9f)
                curveTo(15f, 8.204f, 14.684f, 7.441f, 14.121f, 6.879f)
                curveTo(13.559f, 6.316f, 12.796f, 6f, 12f, 6f)
                curveTo(11.204f, 6f, 10.441f, 6.316f, 9.879f, 6.879f)
                curveTo(9.316f, 7.441f, 9f, 8.204f, 9f, 9f)
                curveTo(9f, 9.796f, 9.316f, 10.559f, 9.879f, 11.121f)
                curveTo(10.441f, 11.684f, 11.204f, 12f, 12f, 12f)
                close()
                moveTo(1f, 18f)
                verticalLineTo(17f)
                curveTo(1f, 16.204f, 1.316f, 15.441f, 1.879f, 14.879f)
                curveTo(2.441f, 14.316f, 3.204f, 14f, 4f, 14f)
                moveTo(4f, 14f)
                curveTo(4.53f, 14f, 5.039f, 13.789f, 5.414f, 13.414f)
                curveTo(5.789f, 13.039f, 6f, 12.53f, 6f, 12f)
                curveTo(6f, 11.47f, 5.789f, 10.961f, 5.414f, 10.586f)
                curveTo(5.039f, 10.211f, 4.53f, 10f, 4f, 10f)
                curveTo(3.47f, 10f, 2.961f, 10.211f, 2.586f, 10.586f)
                curveTo(2.211f, 10.961f, 2f, 11.47f, 2f, 12f)
                curveTo(2f, 12.53f, 2.211f, 13.039f, 2.586f, 13.414f)
                curveTo(2.961f, 13.789f, 3.47f, 14f, 4f, 14f)
                close()
                moveTo(23f, 18f)
                verticalLineTo(17f)
                curveTo(23f, 16.204f, 22.684f, 15.441f, 22.121f, 14.879f)
                curveTo(21.559f, 14.316f, 20.796f, 14f, 20f, 14f)
                moveTo(20f, 14f)
                curveTo(20.53f, 14f, 21.039f, 13.789f, 21.414f, 13.414f)
                curveTo(21.789f, 13.039f, 22f, 12.53f, 22f, 12f)
                curveTo(22f, 11.47f, 21.789f, 10.961f, 21.414f, 10.586f)
                curveTo(21.039f, 10.211f, 20.53f, 10f, 20f, 10f)
                curveTo(19.47f, 10f, 18.961f, 10.211f, 18.586f, 10.586f)
                curveTo(18.211f, 10.961f, 18f, 11.47f, 18f, 12f)
                curveTo(18f, 12.53f, 18.211f, 13.039f, 18.586f, 13.414f)
                curveTo(18.961f, 13.789f, 19.47f, 14f, 20f, 14f)
                close()
            }
        }.build()

        return _Group!!
    }

@Suppress("ObjectPropertyName")
private var _Group: ImageVector? = null
