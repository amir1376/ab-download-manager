package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.OpenSource: ImageVector
    get() {
        if (_OpenSource != null) {
            return _OpenSource!!
        }
        _OpenSource = ImageVector.Builder(
            name = "OpenSource",
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
                moveTo(9f, 19f)
                curveTo(4.7f, 20.4f, 4.7f, 16.5f, 3f, 16f)
                moveTo(15f, 21f)
                verticalLineTo(17.5f)
                curveTo(15f, 16.5f, 15.1f, 16.1f, 14.5f, 15.5f)
                curveTo(17.3f, 15.2f, 20f, 14.1f, 20f, 9.5f)
                curveTo(19.999f, 8.305f, 19.532f, 7.157f, 18.7f, 6.3f)
                curveTo(19.09f, 5.262f, 19.055f, 4.112f, 18.6f, 3.1f)
                curveTo(18.6f, 3.1f, 17.5f, 2.8f, 15.1f, 4.4f)
                curveTo(13.067f, 3.871f, 10.933f, 3.871f, 8.9f, 4.4f)
                curveTo(6.5f, 2.8f, 5.4f, 3.1f, 5.4f, 3.1f)
                curveTo(4.945f, 4.112f, 4.91f, 5.262f, 5.3f, 6.3f)
                curveTo(4.467f, 7.157f, 4.001f, 8.305f, 4f, 9.5f)
                curveTo(4f, 14.1f, 6.7f, 15.2f, 9.5f, 15.5f)
                curveTo(8.9f, 16.1f, 8.9f, 16.7f, 9f, 17.5f)
                verticalLineTo(21f)
            }
        }.build()

        return _OpenSource!!
    }

@Suppress("ObjectPropertyName")
private var _OpenSource: ImageVector? = null
