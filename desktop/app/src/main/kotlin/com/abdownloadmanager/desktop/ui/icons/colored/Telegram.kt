package com.abdownloadmanager.desktop.ui.icons.colored

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Colored.Telegram: ImageVector
    get() {
        if (_Telegram != null) {
            return _Telegram!!
        }
        _Telegram = ImageVector.Builder(
            name = "Colored.Telegram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF2AABEE),
                        1f to Color(0xFF229ED9)
                    ),
                    start = Offset(1200f, 0f),
                    end = Offset(1200f, 2400f)
                )
            ) {
                moveTo(12f, 0f)
                curveTo(8.818f, 0f, 5.764f, 1.265f, 3.516f, 3.515f)
                curveTo(1.265f, 5.765f, 0.001f, 8.817f, 0f, 12f)
                curveTo(0f, 15.181f, 1.266f, 18.236f, 3.516f, 20.485f)
                curveTo(5.764f, 22.735f, 8.818f, 24f, 12f, 24f)
                curveTo(15.182f, 24f, 18.236f, 22.735f, 20.484f, 20.485f)
                curveTo(22.734f, 18.236f, 24f, 15.181f, 24f, 12f)
                curveTo(24f, 8.819f, 22.734f, 5.764f, 20.484f, 3.515f)
                curveTo(18.236f, 1.265f, 15.182f, 0f, 12f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(5.432f, 11.873f)
                curveTo(8.931f, 10.349f, 11.263f, 9.344f, 12.429f, 8.859f)
                curveTo(15.763f, 7.473f, 16.455f, 7.232f, 16.907f, 7.224f)
                curveTo(17.006f, 7.222f, 17.228f, 7.247f, 17.372f, 7.364f)
                curveTo(17.492f, 7.462f, 17.526f, 7.595f, 17.542f, 7.689f)
                curveTo(17.558f, 7.782f, 17.578f, 7.995f, 17.561f, 8.161f)
                curveTo(17.381f, 10.058f, 16.599f, 14.663f, 16.202f, 16.788f)
                curveTo(16.035f, 17.688f, 15.703f, 17.989f, 15.382f, 18.018f)
                curveTo(14.685f, 18.083f, 14.156f, 17.558f, 13.481f, 17.116f)
                curveTo(12.426f, 16.423f, 11.829f, 15.992f, 10.804f, 15.317f)
                curveTo(9.619f, 14.536f, 10.387f, 14.107f, 11.063f, 13.406f)
                curveTo(11.239f, 13.222f, 14.31f, 10.429f, 14.368f, 10.176f)
                curveTo(14.376f, 10.144f, 14.383f, 10.026f, 14.312f, 9.964f)
                curveTo(14.243f, 9.901f, 14.139f, 9.923f, 14.064f, 9.94f)
                curveTo(13.958f, 9.964f, 12.272f, 11.079f, 9.002f, 13.285f)
                curveTo(8.524f, 13.614f, 8.091f, 13.774f, 7.701f, 13.766f)
                curveTo(7.273f, 13.757f, 6.448f, 13.524f, 5.835f, 13.325f)
                curveTo(5.085f, 13.08f, 4.487f, 12.951f, 4.539f, 12.536f)
                curveTo(4.566f, 12.32f, 4.864f, 12.099f, 5.432f, 11.873f)
                close()
            }
        }.build()

        return _Telegram!!
    }

@Suppress("ObjectPropertyName")
private var _Telegram: ImageVector? = null
