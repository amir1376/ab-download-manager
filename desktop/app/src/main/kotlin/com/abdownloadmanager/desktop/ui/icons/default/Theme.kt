package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Theme: ImageVector
    get() {
        if (_Theme != null) {
            return _Theme!!
        }
        _Theme = ImageVector.Builder(
            name = "Default.Theme",
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
                moveTo(8.2f, 13.2f)
                curveTo(9.218f, 10.505f, 10.944f, 8.135f, 13.197f, 6.339f)
                curveTo(15.45f, 4.544f, 18.146f, 3.39f, 21f, 3f)
                curveTo(20.61f, 5.854f, 19.456f, 8.55f, 17.66f, 10.803f)
                curveTo(15.865f, 13.056f, 13.495f, 14.782f, 10.8f, 15.8f)
                moveTo(10.6f, 9f)
                curveTo(12.543f, 9.897f, 14.103f, 11.457f, 15f, 13.4f)
                moveTo(3f, 21f)
                verticalLineTo(17f)
                curveTo(3f, 16.209f, 3.235f, 15.436f, 3.674f, 14.778f)
                curveTo(4.114f, 14.12f, 4.738f, 13.607f, 5.469f, 13.304f)
                curveTo(6.2f, 13.002f, 7.004f, 12.922f, 7.78f, 13.077f)
                curveTo(8.556f, 13.231f, 9.269f, 13.612f, 9.828f, 14.172f)
                curveTo(10.388f, 14.731f, 10.769f, 15.444f, 10.923f, 16.22f)
                curveTo(11.078f, 16.996f, 10.998f, 17.8f, 10.696f, 18.531f)
                curveTo(10.393f, 19.262f, 9.88f, 19.886f, 9.222f, 20.326f)
                curveTo(8.564f, 20.765f, 7.791f, 21f, 7f, 21f)
                horizontalLineTo(3f)
                close()
            }
        }.build()

        return _Theme!!
    }

@Suppress("ObjectPropertyName")
private var _Theme: ImageVector? = null
