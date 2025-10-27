package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Network: ImageVector
    get() {
        if (_Network != null) {
            return _Network!!
        }
        _Network = ImageVector.Builder(
            name = "Network",
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
                moveTo(7f, 3f)
                verticalLineTo(21f)
                moveTo(7f, 3f)
                lineTo(10f, 6f)
                moveTo(7f, 3f)
                lineTo(4f, 6f)
                moveTo(20f, 18f)
                lineTo(17f, 21f)
                moveTo(17f, 21f)
                lineTo(14f, 18f)
                moveTo(17f, 21f)
                verticalLineTo(3f)
            }
        }.build()

        return _Network!!
    }

@Suppress("ObjectPropertyName")
private var _Network: ImageVector? = null
