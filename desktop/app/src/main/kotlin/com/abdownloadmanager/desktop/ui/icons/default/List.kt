package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.List: ImageVector
    get() {
        if (_List != null) {
            return _List!!
        }
        _List = ImageVector.Builder(
            name = "Default.List",
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
                moveTo(9f, 6f)
                horizontalLineTo(20f)
                moveTo(9f, 12f)
                horizontalLineTo(20f)
                moveTo(9f, 18f)
                horizontalLineTo(20f)
                moveTo(5f, 6f)
                verticalLineTo(6.01f)
                moveTo(5f, 12f)
                verticalLineTo(12.01f)
                moveTo(5f, 18f)
                verticalLineTo(18.01f)
            }
        }.build()

        return _List!!
    }

@Suppress("ObjectPropertyName")
private var _List: ImageVector? = null
