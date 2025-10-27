package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Data: ImageVector
    get() {
        if (_Data != null) {
            return _Data!!
        }
        _Data = ImageVector.Builder(
            name = "Data",
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
                moveTo(4f, 6f)
                curveTo(4f, 6.796f, 4.843f, 7.559f, 6.343f, 8.121f)
                curveTo(7.843f, 8.684f, 9.878f, 9f, 12f, 9f)
                curveTo(14.122f, 9f, 16.157f, 8.684f, 17.657f, 8.121f)
                curveTo(19.157f, 7.559f, 20f, 6.796f, 20f, 6f)
                moveTo(4f, 6f)
                curveTo(4f, 5.204f, 4.843f, 4.441f, 6.343f, 3.879f)
                curveTo(7.843f, 3.316f, 9.878f, 3f, 12f, 3f)
                curveTo(14.122f, 3f, 16.157f, 3.316f, 17.657f, 3.879f)
                curveTo(19.157f, 4.441f, 20f, 5.204f, 20f, 6f)
                moveTo(4f, 6f)
                verticalLineTo(12f)
                moveTo(20f, 6f)
                verticalLineTo(12f)
                moveTo(4f, 12f)
                curveTo(4f, 12.796f, 4.843f, 13.559f, 6.343f, 14.121f)
                curveTo(7.843f, 14.684f, 9.878f, 15f, 12f, 15f)
                curveTo(14.122f, 15f, 16.157f, 14.684f, 17.657f, 14.121f)
                curveTo(19.157f, 13.559f, 20f, 12.796f, 20f, 12f)
                moveTo(4f, 12f)
                verticalLineTo(18f)
                curveTo(4f, 18.796f, 4.843f, 19.559f, 6.343f, 20.121f)
                curveTo(7.843f, 20.684f, 9.878f, 21f, 12f, 21f)
                curveTo(14.122f, 21f, 16.157f, 20.684f, 17.657f, 20.121f)
                curveTo(19.157f, 19.559f, 20f, 18.796f, 20f, 18f)
                verticalLineTo(12f)
            }
        }.build()

        return _Data!!
    }

@Suppress("ObjectPropertyName")
private var _Data: ImageVector? = null
