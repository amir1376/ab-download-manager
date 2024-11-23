package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.VerticalDirection: ImageVector
    get() {
        if (_VerticalDirection != null) {
            return _VerticalDirection!!
        }
        _VerticalDirection = ImageVector.Builder(
            name = "Default.VerticalDirection",
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
                moveTo(9f, 10f)
                lineTo(12f, 7f)
                lineTo(15f, 10f)
                moveTo(9f, 14f)
                lineTo(12f, 17f)
                lineTo(15f, 14f)
            }
        }.build()

        return _VerticalDirection!!
    }

@Suppress("ObjectPropertyName")
private var _VerticalDirection: ImageVector? = null
