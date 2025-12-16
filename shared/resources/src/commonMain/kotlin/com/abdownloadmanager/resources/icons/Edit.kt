package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Edit: ImageVector
    get() {
        if (_Edit != null) {
            return _Edit!!
        }
        _Edit = ImageVector.Builder(
            name = "Edit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(3f, 17.46f)
                verticalLineTo(20.5f)
                curveTo(3f, 20.78f, 3.22f, 21f, 3.5f, 21f)
                horizontalLineTo(6.54f)
                curveTo(6.67f, 21f, 6.8f, 20.95f, 6.89f, 20.85f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3.15f, 17.1f)
                curveTo(3.05f, 17.2f, 3f, 17.32f, 3f, 17.46f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(20.803f, 6.947f, 20.876f, 6.838f, 20.926f, 6.717f)
                curveTo(20.977f, 6.596f, 21.002f, 6.466f, 21.002f, 6.335f)
                curveTo(21.002f, 6.204f, 20.977f, 6.074f, 20.926f, 5.953f)
                curveTo(20.876f, 5.832f, 20.803f, 5.723f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(18.278f, 3.197f, 18.168f, 3.124f, 18.047f, 3.074f)
                curveTo(17.926f, 3.023f, 17.796f, 2.998f, 17.665f, 2.998f)
                curveTo(17.534f, 2.998f, 17.404f, 3.023f, 17.283f, 3.074f)
                curveTo(17.162f, 3.124f, 17.052f, 3.197f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }.build()

        return _Edit!!
    }

@Suppress("ObjectPropertyName")
private var _Edit: ImageVector? = null
