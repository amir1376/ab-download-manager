package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Clock: ImageVector
    get() {
        if (_Clock != null) {
            return _Clock!!
        }
        _Clock = ImageVector.Builder(
            name = "Clock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 12f)
                curveTo(20f, 9.878f, 19.157f, 7.843f, 17.657f, 6.343f)
                curveTo(16.157f, 4.842f, 14.122f, 4f, 12f, 4f)
                curveTo(9.878f, 4f, 7.843f, 4.842f, 6.343f, 6.343f)
                curveTo(4.842f, 7.843f, 4f, 9.878f, 4f, 12f)
                curveTo(4f, 13.051f, 4.207f, 14.091f, 4.609f, 15.061f)
                curveTo(5.011f, 16.032f, 5.6f, 16.914f, 6.343f, 17.657f)
                curveTo(7.086f, 18.4f, 7.968f, 18.989f, 8.938f, 19.391f)
                curveTo(9.909f, 19.793f, 10.949f, 20f, 12f, 20f)
                lineTo(12.394f, 19.99f)
                curveTo(13.31f, 19.945f, 14.212f, 19.742f, 15.061f, 19.391f)
                curveTo(16.032f, 18.989f, 16.914f, 18.4f, 17.657f, 17.657f)
                curveTo(18.4f, 16.914f, 18.989f, 16.032f, 19.391f, 15.061f)
                curveTo(19.793f, 14.091f, 20f, 13.051f, 20f, 12f)
                close()
                moveTo(11f, 7f)
                curveTo(11f, 6.448f, 11.448f, 6f, 12f, 6f)
                curveTo(12.552f, 6f, 13f, 6.448f, 13f, 7f)
                verticalLineTo(11.586f)
                lineTo(15.707f, 14.293f)
                curveTo(16.098f, 14.684f, 16.098f, 15.316f, 15.707f, 15.707f)
                curveTo(15.316f, 16.098f, 14.684f, 16.098f, 14.293f, 15.707f)
                lineTo(11.293f, 12.707f)
                curveTo(11.105f, 12.519f, 11f, 12.265f, 11f, 12f)
                verticalLineTo(7f)
                close()
                moveTo(22f, 12f)
                curveTo(22f, 13.313f, 21.742f, 14.614f, 21.239f, 15.827f)
                curveTo(20.737f, 17.04f, 20f, 18.143f, 19.071f, 19.071f)
                curveTo(18.143f, 20f, 17.04f, 20.737f, 15.827f, 21.239f)
                curveTo(14.766f, 21.679f, 13.637f, 21.932f, 12.492f, 21.988f)
                lineTo(12f, 22f)
                curveTo(10.687f, 22f, 9.386f, 21.742f, 8.173f, 21.239f)
                curveTo(6.96f, 20.737f, 5.857f, 20f, 4.929f, 19.071f)
                curveTo(4f, 18.143f, 3.263f, 17.04f, 2.761f, 15.827f)
                curveTo(2.258f, 14.614f, 2f, 13.313f, 2f, 12f)
                curveTo(2f, 9.348f, 3.053f, 6.804f, 4.929f, 4.929f)
                curveTo(6.804f, 3.053f, 9.348f, 2f, 12f, 2f)
                curveTo(14.652f, 2f, 17.196f, 3.053f, 19.071f, 4.929f)
                curveTo(20.947f, 6.804f, 22f, 9.348f, 22f, 12f)
                close()
            }
        }.build()

        return _Clock!!
    }

@Suppress("ObjectPropertyName")
private var _Clock: ImageVector? = null
