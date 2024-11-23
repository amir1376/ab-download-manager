package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Refresh: ImageVector
    get() {
        if (_Refresh != null) {
            return _Refresh!!
        }
        _Refresh = ImageVector.Builder(
            name = "Default.Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 10.873f)
                curveTo(20.725f, 8.889f, 19.806f, 7.052f, 18.386f, 5.643f)
                curveTo(16.966f, 4.233f, 15.123f, 3.331f, 13.14f, 3.075f)
                curveTo(11.158f, 2.819f, 9.147f, 3.223f, 7.416f, 4.224f)
                curveTo(5.685f, 5.226f, 4.331f, 6.77f, 3.563f, 8.619f)
                moveTo(3f, 4.11f)
                verticalLineTo(8.619f)
                horizontalLineTo(7.5f)
                moveTo(3f, 13.127f)
                curveTo(3.275f, 15.111f, 4.194f, 16.948f, 5.614f, 18.358f)
                curveTo(7.034f, 19.767f, 8.877f, 20.669f, 10.86f, 20.925f)
                curveTo(12.842f, 21.181f, 14.853f, 20.777f, 16.584f, 19.776f)
                curveTo(18.315f, 18.774f, 19.669f, 17.23f, 20.438f, 15.381f)
                moveTo(21f, 19.89f)
                verticalLineTo(15.381f)
                horizontalLineTo(16.5f)
            }
        }.build()

        return _Refresh!!
    }

@Suppress("ObjectPropertyName")
private var _Refresh: ImageVector? = null
