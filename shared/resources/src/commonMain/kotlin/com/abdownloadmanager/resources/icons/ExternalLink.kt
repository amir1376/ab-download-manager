package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.ExternalLink: ImageVector
    get() {
        if (_ExternalLink != null) {
            return _ExternalLink!!
        }
        _ExternalLink = ImageVector.Builder(
            name = "ExternalLink",
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
                moveTo(12f, 6f)
                horizontalLineTo(6f)
                curveTo(5.47f, 6f, 4.961f, 6.211f, 4.586f, 6.586f)
                curveTo(4.211f, 6.961f, 4f, 7.47f, 4f, 8f)
                verticalLineTo(18f)
                curveTo(4f, 18.53f, 4.211f, 19.039f, 4.586f, 19.414f)
                curveTo(4.961f, 19.789f, 5.47f, 20f, 6f, 20f)
                horizontalLineTo(16f)
                curveTo(16.53f, 20f, 17.039f, 19.789f, 17.414f, 19.414f)
                curveTo(17.789f, 19.039f, 18f, 18.53f, 18f, 18f)
                verticalLineTo(12f)
                moveTo(11f, 13f)
                lineTo(20f, 4f)
                moveTo(20f, 4f)
                horizontalLineTo(15f)
                moveTo(20f, 4f)
                verticalLineTo(9f)
            }
        }.build()

        return _ExternalLink!!
    }

@Suppress("ObjectPropertyName")
private var _ExternalLink: ImageVector? = null
