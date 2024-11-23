package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Share: ImageVector
    get() {
        if (_Share != null) {
            return _Share!!
        }
        _Share = ImageVector.Builder(
            name = "Default.Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12.69f, 3.317f)
                curveTo(12.958f, 3.195f, 13.272f, 3.242f, 13.494f, 3.436f)
                lineTo(21.494f, 10.436f)
                curveTo(21.657f, 10.578f, 21.75f, 10.784f, 21.75f, 11f)
                curveTo(21.75f, 11.216f, 21.657f, 11.422f, 21.494f, 11.564f)
                lineTo(13.494f, 18.564f)
                curveTo(13.272f, 18.758f, 12.958f, 18.805f, 12.69f, 18.683f)
                curveTo(12.422f, 18.561f, 12.25f, 18.294f, 12.25f, 18f)
                verticalLineTo(14.816f)
                curveTo(11.47f, 14.941f, 10.65f, 15.237f, 9.821f, 15.653f)
                curveTo(8.725f, 16.203f, 7.66f, 16.938f, 6.72f, 17.68f)
                curveTo(5.781f, 18.421f, 4.982f, 19.156f, 4.416f, 19.697f)
                curveTo(4.219f, 19.885f, 4.055f, 20.046f, 3.919f, 20.179f)
                curveTo(3.857f, 20.239f, 3.802f, 20.293f, 3.752f, 20.341f)
                curveTo(3.677f, 20.414f, 3.605f, 20.483f, 3.549f, 20.532f)
                lineTo(3.547f, 20.534f)
                curveTo(3.528f, 20.55f, 3.47f, 20.601f, 3.4f, 20.643f)
                curveTo(3.379f, 20.656f, 3.338f, 20.679f, 3.284f, 20.701f)
                curveTo(3.246f, 20.716f, 3.117f, 20.766f, 2.946f, 20.753f)
                curveTo(2.716f, 20.736f, 2.446f, 20.601f, 2.315f, 20.31f)
                curveTo(2.222f, 20.101f, 2.253f, 19.917f, 2.262f, 19.867f)
                lineTo(2.263f, 19.861f)
                lineTo(2.263f, 19.861f)
                curveTo(3.223f, 14.755f, 5.645f, 8.745f, 12.25f, 7.374f)
                verticalLineTo(4f)
                curveTo(12.25f, 3.706f, 12.422f, 3.439f, 12.69f, 3.317f)
                close()
                moveTo(13.75f, 5.653f)
                verticalLineTo(8f)
                curveTo(13.75f, 8.37f, 13.481f, 8.684f, 13.116f, 8.741f)
                curveTo(7.983f, 9.543f, 5.515f, 13.458f, 4.29f, 17.769f)
                curveTo(4.731f, 17.374f, 5.237f, 16.94f, 5.791f, 16.503f)
                curveTo(6.778f, 15.724f, 7.931f, 14.923f, 9.149f, 14.312f)
                curveTo(10.361f, 13.704f, 11.682f, 13.261f, 12.994f, 13.25f)
                curveTo(13.194f, 13.248f, 13.386f, 13.327f, 13.528f, 13.467f)
                curveTo(13.67f, 13.608f, 13.75f, 13.8f, 13.75f, 14f)
                verticalLineTo(16.347f)
                lineTo(19.861f, 11f)
                lineTo(13.75f, 5.653f)
                close()
                moveTo(2.555f, 19.408f)
                curveTo(2.555f, 19.408f, 2.557f, 19.406f, 2.561f, 19.403f)
                curveTo(2.557f, 19.406f, 2.555f, 19.408f, 2.555f, 19.408f)
                close()
            }
        }.build()

        return _Share!!
    }

@Suppress("ObjectPropertyName")
private var _Share: ImageVector? = null
