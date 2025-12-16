package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Fast: ImageVector
    get() {
        if (_Fast != null) {
            return _Fast!!
        }
        _Fast = ImageVector.Builder(
            name = "Fast",
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
                moveTo(13f, 3f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                lineTo(11f, 21f)
                verticalLineTo(14f)
                horizontalLineTo(5f)
                lineTo(13f, 3f)
                close()
            }
        }.build()

        return _Fast!!
    }

@Suppress("ObjectPropertyName")
private var _Fast: ImageVector? = null
