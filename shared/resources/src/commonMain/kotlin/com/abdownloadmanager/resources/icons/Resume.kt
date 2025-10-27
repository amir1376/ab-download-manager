package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Resume: ImageVector
    get() {
        if (_Resume != null) {
            return _Resume!!
        }
        _Resume = ImageVector.Builder(
            name = "Resume",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6.634f, 3.345f)
                curveTo(6.871f, 3.213f, 7.162f, 3.219f, 7.393f, 3.361f)
                lineTo(20.393f, 11.361f)
                curveTo(20.615f, 11.498f, 20.75f, 11.74f, 20.75f, 12f)
                curveTo(20.75f, 12.26f, 20.615f, 12.502f, 20.393f, 12.639f)
                lineTo(7.393f, 20.639f)
                curveTo(7.162f, 20.781f, 6.871f, 20.787f, 6.634f, 20.655f)
                curveTo(6.397f, 20.522f, 6.25f, 20.272f, 6.25f, 20f)
                verticalLineTo(4f)
                curveTo(6.25f, 3.728f, 6.397f, 3.478f, 6.634f, 3.345f)
                close()
                moveTo(7.75f, 5.342f)
                verticalLineTo(18.658f)
                lineTo(18.569f, 12f)
                lineTo(7.75f, 5.342f)
                close()
            }
        }.build()

        return _Resume!!
    }

@Suppress("ObjectPropertyName")
private var _Resume: ImageVector? = null
