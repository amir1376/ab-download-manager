package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Earth: ImageVector
    get() {
        if (_Earth != null) {
            return _Earth!!
        }
        _Earth = ImageVector.Builder(
            name = "Earth",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.6f, 9f)
                horizontalLineTo(20.4f)
                moveTo(3.6f, 15f)
                horizontalLineTo(20.4f)
                moveTo(11.5f, 3f)
                curveTo(9.815f, 5.7f, 8.922f, 8.818f, 8.922f, 12f)
                curveTo(8.922f, 15.182f, 9.815f, 18.3f, 11.5f, 21f)
                moveTo(12.5f, 3f)
                curveTo(14.185f, 5.7f, 15.078f, 8.818f, 15.078f, 12f)
                curveTo(15.078f, 15.182f, 14.185f, 18.3f, 12.5f, 21f)
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

        return _Earth!!
    }

@Suppress("ObjectPropertyName")
private var _Earth: ImageVector? = null
