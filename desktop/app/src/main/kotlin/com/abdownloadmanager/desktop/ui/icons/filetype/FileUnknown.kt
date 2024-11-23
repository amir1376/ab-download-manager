package com.abdownloadmanager.desktop.ui.icons.filetype

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.FileType.FileUnknown: ImageVector
    get() {
        if (_FileUnknown != null) {
            return _FileUnknown!!
        }
        _FileUnknown = ImageVector.Builder(
            name = "FileType.FileUnknown",
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
                moveTo(14f, 3f)
                verticalLineTo(7f)
                curveTo(14f, 7.265f, 14.105f, 7.52f, 14.293f, 7.707f)
                curveTo(14.48f, 7.895f, 14.735f, 8f, 15f, 8f)
                horizontalLineTo(19f)
                moveTo(14f, 3f)
                horizontalLineTo(7f)
                curveTo(6.47f, 3f, 5.961f, 3.211f, 5.586f, 3.586f)
                curveTo(5.211f, 3.961f, 5f, 4.47f, 5f, 5f)
                verticalLineTo(19f)
                curveTo(5f, 19.53f, 5.211f, 20.039f, 5.586f, 20.414f)
                curveTo(5.961f, 20.789f, 6.47f, 21f, 7f, 21f)
                horizontalLineTo(17f)
                curveTo(17.53f, 21f, 18.039f, 20.789f, 18.414f, 20.414f)
                curveTo(18.789f, 20.039f, 19f, 19.53f, 19f, 19f)
                verticalLineTo(8f)
                moveTo(14f, 3f)
                lineTo(19f, 8f)
                moveTo(12f, 17f)
                verticalLineTo(17.01f)
                moveTo(12f, 14f)
                curveTo(12.252f, 14f, 12.499f, 13.937f, 12.72f, 13.816f)
                curveTo(12.941f, 13.695f, 13.128f, 13.521f, 13.264f, 13.309f)
                curveTo(13.4f, 13.097f, 13.48f, 12.854f, 13.497f, 12.603f)
                curveTo(13.514f, 12.352f, 13.468f, 12.101f, 13.363f, 11.872f)
                curveTo(13.258f, 11.644f, 13.097f, 11.445f, 12.894f, 11.295f)
                curveTo(12.692f, 11.145f, 12.456f, 11.049f, 12.206f, 11.014f)
                curveTo(11.957f, 10.98f, 11.703f, 11.009f, 11.468f, 11.098f)
                curveTo(11.232f, 11.187f, 11.023f, 11.335f, 10.86f, 11.526f)
            }
        }.build()

        return _FileUnknown!!
    }

@Suppress("ObjectPropertyName")
private var _FileUnknown: ImageVector? = null
