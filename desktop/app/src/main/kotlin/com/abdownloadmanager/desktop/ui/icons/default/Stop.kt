package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Stop: ImageVector
    get() {
        if (_Stop != null) {
            return _Stop!!
        }
        _Stop = ImageVector.Builder(
            name = "Default.Stop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(7f, 5.75f)
                curveTo(6.668f, 5.75f, 6.351f, 5.882f, 6.116f, 6.116f)
                curveTo(5.882f, 6.351f, 5.75f, 6.668f, 5.75f, 7f)
                verticalLineTo(17f)
                curveTo(5.75f, 17.331f, 5.882f, 17.649f, 6.116f, 17.884f)
                curveTo(6.351f, 18.118f, 6.668f, 18.25f, 7f, 18.25f)
                horizontalLineTo(17f)
                curveTo(17.331f, 18.25f, 17.649f, 18.118f, 17.884f, 17.884f)
                curveTo(18.118f, 17.649f, 18.25f, 17.331f, 18.25f, 17f)
                verticalLineTo(7f)
                curveTo(18.25f, 6.668f, 18.118f, 6.351f, 17.884f, 6.116f)
                curveTo(17.649f, 5.882f, 17.331f, 5.75f, 17f, 5.75f)
                horizontalLineTo(7f)
                close()
                moveTo(5.055f, 5.055f)
                curveTo(5.571f, 4.54f, 6.271f, 4.25f, 7f, 4.25f)
                horizontalLineTo(17f)
                curveTo(17.729f, 4.25f, 18.429f, 4.54f, 18.944f, 5.055f)
                curveTo(19.46f, 5.571f, 19.75f, 6.271f, 19.75f, 7f)
                verticalLineTo(17f)
                curveTo(19.75f, 17.729f, 19.46f, 18.429f, 18.944f, 18.944f)
                curveTo(18.429f, 19.46f, 17.729f, 19.75f, 17f, 19.75f)
                horizontalLineTo(7f)
                curveTo(6.271f, 19.75f, 5.571f, 19.46f, 5.055f, 18.944f)
                curveTo(4.54f, 18.429f, 4.25f, 17.729f, 4.25f, 17f)
                verticalLineTo(7f)
                curveTo(4.25f, 6.271f, 4.54f, 5.571f, 5.055f, 5.055f)
                close()
            }
        }.build()

        return _Stop!!
    }

@Suppress("ObjectPropertyName")
private var _Stop: ImageVector? = null
