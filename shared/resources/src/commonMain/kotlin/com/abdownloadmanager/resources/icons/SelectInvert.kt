package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.SelectInvert: ImageVector
    get() {
        if (_SelectInvert != null) {
            return _SelectInvert!!
        }
        _SelectInvert = ImageVector.Builder(
            name = "SelectInvert",
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
                moveTo(4f, 4f)
                verticalLineTo(4.01f)
                moveTo(8f, 4f)
                verticalLineTo(4.01f)
                moveTo(12f, 4f)
                verticalLineTo(4.01f)
                moveTo(16f, 4f)
                verticalLineTo(4.01f)
                moveTo(20f, 4f)
                verticalLineTo(4.01f)
                moveTo(4f, 8f)
                verticalLineTo(8.01f)
                moveTo(12f, 8f)
                verticalLineTo(8.01f)
                moveTo(20f, 8f)
                verticalLineTo(8.01f)
                moveTo(4f, 12f)
                verticalLineTo(12.01f)
                moveTo(8f, 12f)
                verticalLineTo(12.01f)
                moveTo(12f, 12f)
                verticalLineTo(12.01f)
                moveTo(16f, 12f)
                verticalLineTo(12.01f)
                moveTo(20f, 12f)
                verticalLineTo(12.01f)
                moveTo(4f, 16f)
                verticalLineTo(16.01f)
                moveTo(12f, 16f)
                verticalLineTo(16.01f)
                moveTo(20f, 16f)
                verticalLineTo(16.01f)
                moveTo(4f, 20f)
                verticalLineTo(20.01f)
                moveTo(8f, 20f)
                verticalLineTo(20.01f)
                moveTo(12f, 20f)
                verticalLineTo(20.01f)
                moveTo(16f, 20f)
                verticalLineTo(20.01f)
                moveTo(20f, 20f)
                verticalLineTo(20.01f)
            }
        }.build()

        return _SelectInvert!!
    }

@Suppress("ObjectPropertyName")
private var _SelectInvert: ImageVector? = null
