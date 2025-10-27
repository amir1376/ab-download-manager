package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.StopAll: ImageVector
    get() {
        if (_StopAll != null) {
            return _StopAll!!
        }
        _StopAll = ImageVector.Builder(
            name = "StopAll",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10.098f, 2.437f)
                curveTo(11.989f, 2.061f, 13.95f, 2.254f, 15.731f, 2.992f)
                curveTo(17.513f, 3.73f, 19.035f, 4.98f, 20.107f, 6.583f)
                curveTo(21.178f, 8.187f, 21.75f, 10.072f, 21.75f, 12f)
                curveTo(21.75f, 12.414f, 21.414f, 12.75f, 21f, 12.75f)
                curveTo(20.586f, 12.75f, 20.25f, 12.414f, 20.25f, 12f)
                curveTo(20.25f, 10.368f, 19.766f, 8.773f, 18.86f, 7.417f)
                curveTo(17.953f, 6.06f, 16.665f, 5.002f, 15.157f, 4.378f)
                curveTo(13.65f, 3.754f, 11.991f, 3.59f, 10.391f, 3.909f)
                curveTo(8.79f, 4.227f, 7.32f, 5.013f, 6.166f, 6.166f)
                curveTo(5.013f, 7.32f, 4.227f, 8.79f, 3.909f, 10.391f)
                curveTo(3.59f, 11.991f, 3.754f, 13.65f, 4.378f, 15.157f)
                curveTo(5.002f, 16.665f, 6.06f, 17.953f, 7.417f, 18.86f)
                curveTo(8.773f, 19.766f, 10.368f, 20.25f, 12f, 20.25f)
                curveTo(12.414f, 20.25f, 12.75f, 20.586f, 12.75f, 21f)
                curveTo(12.75f, 21.414f, 12.414f, 21.75f, 12f, 21.75f)
                curveTo(10.072f, 21.75f, 8.187f, 21.178f, 6.583f, 20.107f)
                curveTo(4.98f, 19.035f, 3.73f, 17.513f, 2.992f, 15.731f)
                curveTo(2.254f, 13.95f, 2.061f, 11.989f, 2.437f, 10.098f)
                curveTo(2.814f, 8.207f, 3.742f, 6.469f, 5.106f, 5.106f)
                curveTo(6.469f, 3.742f, 8.207f, 2.814f, 10.098f, 2.437f)
                close()
                moveTo(12f, 6.25f)
                curveTo(12.414f, 6.25f, 12.75f, 6.586f, 12.75f, 7f)
                verticalLineTo(11.689f)
                lineTo(13.53f, 12.47f)
                curveTo(13.823f, 12.763f, 13.823f, 13.237f, 13.53f, 13.53f)
                curveTo(13.237f, 13.823f, 12.763f, 13.823f, 12.47f, 13.53f)
                lineTo(11.47f, 12.53f)
                curveTo(11.329f, 12.39f, 11.25f, 12.199f, 11.25f, 12f)
                verticalLineTo(7f)
                curveTo(11.25f, 6.586f, 11.586f, 6.25f, 12f, 6.25f)
                close()
                moveTo(15.25f, 16f)
                curveTo(15.25f, 15.586f, 15.586f, 15.25f, 16f, 15.25f)
                horizontalLineTo(22f)
                curveTo(22.414f, 15.25f, 22.75f, 15.586f, 22.75f, 16f)
                verticalLineTo(22f)
                curveTo(22.75f, 22.414f, 22.414f, 22.75f, 22f, 22.75f)
                horizontalLineTo(16f)
                curveTo(15.586f, 22.75f, 15.25f, 22.414f, 15.25f, 22f)
                verticalLineTo(16f)
                close()
                moveTo(16.75f, 16.75f)
                verticalLineTo(21.25f)
                horizontalLineTo(21.25f)
                verticalLineTo(16.75f)
                horizontalLineTo(16.75f)
                close()
            }
        }.build()

        return _StopAll!!
    }

@Suppress("ObjectPropertyName")
private var _StopAll: ImageVector? = null
