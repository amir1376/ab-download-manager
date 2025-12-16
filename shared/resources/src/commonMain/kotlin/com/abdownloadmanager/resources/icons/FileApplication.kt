package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.FileApplication: ImageVector
    get() {
        if (_FileApplication != null) {
            return _FileApplication!!
        }
        _FileApplication = ImageVector.Builder(
            name = "FileApplication",
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
                moveTo(14f, 7f)
                horizontalLineTo(20f)
                moveTo(17f, 4f)
                verticalLineTo(10f)
                moveTo(4f, 5f)
                curveTo(4f, 4.735f, 4.105f, 4.48f, 4.293f, 4.293f)
                curveTo(4.48f, 4.105f, 4.735f, 4f, 5f, 4f)
                horizontalLineTo(9f)
                curveTo(9.265f, 4f, 9.52f, 4.105f, 9.707f, 4.293f)
                curveTo(9.895f, 4.48f, 10f, 4.735f, 10f, 5f)
                verticalLineTo(9f)
                curveTo(10f, 9.265f, 9.895f, 9.52f, 9.707f, 9.707f)
                curveTo(9.52f, 9.895f, 9.265f, 10f, 9f, 10f)
                horizontalLineTo(5f)
                curveTo(4.735f, 10f, 4.48f, 9.895f, 4.293f, 9.707f)
                curveTo(4.105f, 9.52f, 4f, 9.265f, 4f, 9f)
                verticalLineTo(5f)
                close()
                moveTo(4f, 15f)
                curveTo(4f, 14.735f, 4.105f, 14.48f, 4.293f, 14.293f)
                curveTo(4.48f, 14.105f, 4.735f, 14f, 5f, 14f)
                horizontalLineTo(9f)
                curveTo(9.265f, 14f, 9.52f, 14.105f, 9.707f, 14.293f)
                curveTo(9.895f, 14.48f, 10f, 14.735f, 10f, 15f)
                verticalLineTo(19f)
                curveTo(10f, 19.265f, 9.895f, 19.52f, 9.707f, 19.707f)
                curveTo(9.52f, 19.895f, 9.265f, 20f, 9f, 20f)
                horizontalLineTo(5f)
                curveTo(4.735f, 20f, 4.48f, 19.895f, 4.293f, 19.707f)
                curveTo(4.105f, 19.52f, 4f, 19.265f, 4f, 19f)
                verticalLineTo(15f)
                close()
                moveTo(14f, 15f)
                curveTo(14f, 14.735f, 14.105f, 14.48f, 14.293f, 14.293f)
                curveTo(14.48f, 14.105f, 14.735f, 14f, 15f, 14f)
                horizontalLineTo(19f)
                curveTo(19.265f, 14f, 19.52f, 14.105f, 19.707f, 14.293f)
                curveTo(19.895f, 14.48f, 20f, 14.735f, 20f, 15f)
                verticalLineTo(19f)
                curveTo(20f, 19.265f, 19.895f, 19.52f, 19.707f, 19.707f)
                curveTo(19.52f, 19.895f, 19.265f, 20f, 19f, 20f)
                horizontalLineTo(15f)
                curveTo(14.735f, 20f, 14.48f, 19.895f, 14.293f, 19.707f)
                curveTo(14.105f, 19.52f, 14f, 19.265f, 14f, 19f)
                verticalLineTo(15f)
                close()
            }
        }.build()

        return _FileApplication!!
    }

@Suppress("ObjectPropertyName")
private var _FileApplication: ImageVector? = null
