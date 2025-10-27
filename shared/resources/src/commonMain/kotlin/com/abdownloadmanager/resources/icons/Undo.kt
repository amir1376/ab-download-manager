package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Undo: ImageVector
    get() {
        if (_Undo != null) {
            return _Undo!!
        }
        _Undo = ImageVector.Builder(
            name = "Undo",
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
                moveTo(9f, 14f)
                lineTo(5f, 10f)
                moveTo(5f, 10f)
                lineTo(9f, 6f)
                moveTo(5f, 10f)
                horizontalLineTo(16f)
                curveTo(17.061f, 10f, 18.078f, 10.421f, 18.828f, 11.172f)
                curveTo(19.579f, 11.922f, 20f, 12.939f, 20f, 14f)
                curveTo(20f, 15.061f, 19.579f, 16.078f, 18.828f, 16.828f)
                curveTo(18.078f, 17.579f, 17.061f, 18f, 16f, 18f)
                horizontalLineTo(15f)
            }
        }.build()

        return _Undo!!
    }

@Suppress("ObjectPropertyName")
private var _Undo: ImageVector? = null
