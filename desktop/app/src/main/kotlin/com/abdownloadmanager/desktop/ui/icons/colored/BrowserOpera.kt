package com.abdownloadmanager.desktop.ui.icons.colored

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Colored.BrowserOpera: ImageVector
    get() {
        if (_BrowserOpera != null) {
            return _BrowserOpera!!
        }
        _BrowserOpera = ImageVector.Builder(
            name = "Colored.BrowserOpera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.3f to Color(0xFFFF1B2D),
                        0.438f to Color(0xFFFA1A2C),
                        0.594f to Color(0xFFED1528),
                        0.758f to Color(0xFFD60E21),
                        0.927f to Color(0xFFB70519),
                        1f to Color(0xFFA70014)
                    ),
                    start = Offset(999.904f, 39.12f),
                    end = Offset(999.904f, 2365.08f)
                )
            ) {
                moveTo(8.053f, 18.759f)
                curveTo(6.722f, 17.194f, 5.869f, 14.878f, 5.813f, 12.281f)
                verticalLineTo(11.719f)
                curveTo(5.869f, 9.122f, 6.731f, 6.806f, 8.053f, 5.241f)
                curveTo(9.778f, 3.009f, 12.309f, 2.006f, 15.169f, 2.006f)
                curveTo(16.931f, 2.006f, 18.591f, 2.128f, 19.997f, 3.066f)
                curveTo(17.888f, 1.163f, 15.103f, 0.009f, 12.047f, 0f)
                horizontalLineTo(12f)
                curveTo(5.372f, 0f, 0f, 5.372f, 0f, 12f)
                curveTo(0f, 18.431f, 5.063f, 23.691f, 11.428f, 23.991f)
                curveTo(11.616f, 24f, 11.813f, 24f, 12f, 24f)
                curveTo(15.075f, 24f, 17.878f, 22.847f, 19.997f, 20.944f)
                curveTo(18.591f, 21.881f, 17.025f, 21.919f, 15.262f, 21.919f)
                curveTo(12.413f, 21.928f, 9.769f, 21f, 8.053f, 18.759f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF9C0000),
                        0.7f to Color(0xFFFF4B4B)
                    ),
                    start = Offset(805.237f, 19.369f),
                    end = Offset(805.237f, 2076.56f)
                )
            ) {
                moveTo(8.053f, 5.241f)
                curveTo(9.15f, 3.937f, 10.575f, 3.159f, 12.131f, 3.159f)
                curveTo(15.628f, 3.159f, 18.459f, 7.116f, 18.459f, 12.009f)
                curveTo(18.459f, 16.903f, 15.628f, 20.859f, 12.131f, 20.859f)
                curveTo(10.575f, 20.859f, 9.159f, 20.072f, 8.053f, 18.778f)
                curveTo(9.778f, 21.009f, 12.337f, 22.434f, 15.188f, 22.434f)
                curveTo(16.941f, 22.434f, 18.591f, 21.9f, 19.997f, 20.962f)
                curveTo(22.453f, 18.75f, 24f, 15.553f, 24f, 12f)
                curveTo(24f, 8.447f, 22.453f, 5.25f, 19.997f, 3.056f)
                curveTo(18.591f, 2.119f, 16.95f, 1.584f, 15.188f, 1.584f)
                curveTo(12.328f, 1.584f, 9.769f, 3f, 8.053f, 5.241f)
                close()
            }
        }.build()

        return _BrowserOpera!!
    }

@Suppress("ObjectPropertyName")
private var _BrowserOpera: ImageVector? = null
