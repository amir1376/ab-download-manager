package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.FolderFinished: ImageVector
    get() {
        if (_FolderFinished != null) {
            return _FolderFinished!!
        }
        _FolderFinished = ImageVector.Builder(
            name = "FolderFinished",
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
                moveTo(11f, 19f)
                horizontalLineTo(5f)
                curveTo(4.47f, 19f, 3.961f, 18.789f, 3.586f, 18.414f)
                curveTo(3.211f, 18.039f, 3f, 17.53f, 3f, 17f)
                verticalLineTo(6f)
                curveTo(3f, 5.47f, 3.211f, 4.961f, 3.586f, 4.586f)
                curveTo(3.961f, 4.211f, 4.47f, 4f, 5f, 4f)
                horizontalLineTo(9f)
                lineTo(12f, 7f)
                horizontalLineTo(19f)
                curveTo(19.53f, 7f, 20.039f, 7.211f, 20.414f, 7.586f)
                curveTo(20.789f, 7.961f, 21f, 8.47f, 21f, 9f)
                verticalLineTo(13f)
                moveTo(15f, 19f)
                lineTo(17f, 21f)
                lineTo(21f, 17f)
            }
        }.build()

        return _FolderFinished!!
    }

@Suppress("ObjectPropertyName")
private var _FolderFinished: ImageVector? = null
