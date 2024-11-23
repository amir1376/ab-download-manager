package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Plus: ImageVector
    get() {
        if (_Plus != null) {
            return _Plus!!
        }
        _Plus = ImageVector.Builder(
            name = "Default.Plus",
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
                moveTo(12f, 5f)
                verticalLineTo(19f)
                moveTo(5f, 12f)
                horizontalLineTo(19f)
            }
        }.build()

        return _Plus!!
    }

@Suppress("ObjectPropertyName")
private var _Plus: ImageVector? = null
