package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Info: ImageVector
    get() {
        if (_Info != null) {
            return _Info!!
        }
        _Info = ImageVector.Builder(
            name = "Default.Info",
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
                moveTo(12f, 9f)
                horizontalLineTo(12.01f)
                moveTo(11f, 12f)
                horizontalLineTo(12f)
                verticalLineTo(16f)
                horizontalLineTo(13f)
                moveTo(3f, 12f)
                curveTo(3f, 13.182f, 3.233f, 14.352f, 3.685f, 15.444f)
                curveTo(4.137f, 16.536f, 4.8f, 17.528f, 5.636f, 18.364f)
                curveTo(6.472f, 19.2f, 7.464f, 19.863f, 8.556f, 20.315f)
                curveTo(9.648f, 20.767f, 10.818f, 21f, 12f, 21f)
                curveTo(13.182f, 21f, 14.352f, 20.767f, 15.444f, 20.315f)
                curveTo(16.536f, 19.863f, 17.528f, 19.2f, 18.364f, 18.364f)
                curveTo(19.2f, 17.528f, 19.863f, 16.536f, 20.315f, 15.444f)
                curveTo(20.767f, 14.352f, 21f, 13.182f, 21f, 12f)
                curveTo(21f, 9.613f, 20.052f, 7.324f, 18.364f, 5.636f)
                curveTo(16.676f, 3.948f, 14.387f, 3f, 12f, 3f)
                curveTo(9.613f, 3f, 7.324f, 3.948f, 5.636f, 5.636f)
                curveTo(3.948f, 7.324f, 3f, 9.613f, 3f, 12f)
                close()
            }
        }.build()

        return _Info!!
    }

@Suppress("ObjectPropertyName")
private var _Info: ImageVector? = null
