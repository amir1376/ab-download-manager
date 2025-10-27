package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.FolderUnfinished: ImageVector
    get() {
        if (_FolderUnfinished != null) {
            return _FolderUnfinished!!
        }
        _FolderUnfinished = ImageVector.Builder(
            name = "FolderUnfinished",
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
                moveTo(12f, 19f)
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
                verticalLineTo(12.5f)
                moveTo(19f, 16f)
                verticalLineTo(22f)
                moveTo(19f, 22f)
                lineTo(22f, 19f)
                moveTo(19f, 22f)
                lineTo(16f, 19f)
            }
        }.build()

        return _FolderUnfinished!!
    }

@Suppress("ObjectPropertyName")
private var _FolderUnfinished: ImageVector? = null
