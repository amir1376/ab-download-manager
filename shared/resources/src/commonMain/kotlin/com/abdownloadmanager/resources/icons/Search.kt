package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Search: ImageVector
    get() {
        if (_Search != null) {
            return _Search!!
        }
        _Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(7.034f, 2.84f)
                curveTo(7.974f, 2.45f, 8.982f, 2.25f, 10f, 2.25f)
                curveTo(11.018f, 2.25f, 12.026f, 2.45f, 12.966f, 2.84f)
                curveTo(13.906f, 3.229f, 14.76f, 3.8f, 15.48f, 4.52f)
                curveTo(16.2f, 5.24f, 16.771f, 6.094f, 17.16f, 7.034f)
                curveTo(17.549f, 7.974f, 17.75f, 8.982f, 17.75f, 10f)
                curveTo(17.75f, 11.018f, 17.549f, 12.026f, 17.16f, 12.966f)
                curveTo(16.867f, 13.674f, 16.47f, 14.334f, 15.985f, 14.924f)
                lineTo(21.53f, 20.47f)
                curveTo(21.823f, 20.763f, 21.823f, 21.237f, 21.53f, 21.53f)
                curveTo(21.237f, 21.823f, 20.763f, 21.823f, 20.47f, 21.53f)
                lineTo(14.924f, 15.985f)
                curveTo(14.334f, 16.47f, 13.674f, 16.867f, 12.966f, 17.16f)
                curveTo(12.026f, 17.549f, 11.018f, 17.75f, 10f, 17.75f)
                curveTo(8.982f, 17.75f, 7.974f, 17.549f, 7.034f, 17.16f)
                curveTo(6.094f, 16.771f, 5.24f, 16.2f, 4.52f, 15.48f)
                curveTo(3.8f, 14.76f, 3.229f, 13.906f, 2.84f, 12.966f)
                curveTo(2.45f, 12.026f, 2.25f, 11.018f, 2.25f, 10f)
                curveTo(2.25f, 8.982f, 2.45f, 7.974f, 2.84f, 7.034f)
                curveTo(3.229f, 6.094f, 3.8f, 5.24f, 4.52f, 4.52f)
                curveTo(5.24f, 3.8f, 6.094f, 3.229f, 7.034f, 2.84f)
                close()
                moveTo(10f, 3.75f)
                curveTo(9.179f, 3.75f, 8.367f, 3.912f, 7.608f, 4.226f)
                curveTo(6.85f, 4.54f, 6.161f, 5f, 5.581f, 5.581f)
                curveTo(5f, 6.161f, 4.54f, 6.85f, 4.226f, 7.608f)
                curveTo(3.912f, 8.367f, 3.75f, 9.179f, 3.75f, 10f)
                curveTo(3.75f, 10.821f, 3.912f, 11.634f, 4.226f, 12.392f)
                curveTo(4.54f, 13.15f, 5f, 13.839f, 5.581f, 14.419f)
                curveTo(6.161f, 15f, 6.85f, 15.46f, 7.608f, 15.774f)
                curveTo(8.367f, 16.088f, 9.179f, 16.25f, 10f, 16.25f)
                curveTo(10.821f, 16.25f, 11.634f, 16.088f, 12.392f, 15.774f)
                curveTo(13.15f, 15.46f, 13.839f, 15f, 14.419f, 14.419f)
                curveTo(15f, 13.839f, 15.46f, 13.15f, 15.774f, 12.392f)
                curveTo(16.088f, 11.634f, 16.25f, 10.821f, 16.25f, 10f)
                curveTo(16.25f, 9.179f, 16.088f, 8.367f, 15.774f, 7.608f)
                curveTo(15.46f, 6.85f, 15f, 6.161f, 14.419f, 5.581f)
                curveTo(13.839f, 5f, 13.15f, 4.54f, 12.392f, 4.226f)
                curveTo(11.634f, 3.912f, 10.821f, 3.75f, 10f, 3.75f)
                close()
            }
        }.build()

        return _Search!!
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
