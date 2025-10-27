package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.DragAndDrop: ImageVector
    get() {
        if (_DragAndDrop != null) {
            return _DragAndDrop!!
        }
        _DragAndDrop = ImageVector.Builder(
            name = "DragAndDrop",
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
                moveTo(18f, 9f)
                lineTo(21f, 12f)
                moveTo(21f, 12f)
                lineTo(18f, 15f)
                moveTo(21f, 12f)
                horizontalLineTo(15f)
                moveTo(6f, 9f)
                lineTo(3f, 12f)
                moveTo(3f, 12f)
                lineTo(6f, 15f)
                moveTo(3f, 12f)
                horizontalLineTo(9f)
                moveTo(9f, 18f)
                lineTo(12f, 21f)
                moveTo(12f, 21f)
                lineTo(15f, 18f)
                moveTo(12f, 21f)
                verticalLineTo(15f)
                moveTo(15f, 6f)
                lineTo(12f, 3f)
                moveTo(12f, 3f)
                lineTo(9f, 6f)
                moveTo(12f, 3f)
                verticalLineTo(9f)
            }
        }.build()

        return _DragAndDrop!!
    }

@Suppress("ObjectPropertyName")
private var _DragAndDrop: ImageVector? = null
