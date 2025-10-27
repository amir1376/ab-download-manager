package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Hearth: ImageVector
    get() {
        if (_Hearth != null) {
            return _Hearth!!
        }
        _Hearth = ImageVector.Builder(
            name = "Hearth",
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
                moveTo(19.5f, 12.572f)
                lineTo(12f, 20f)
                lineTo(4.5f, 12.572f)
                curveTo(4.005f, 12.091f, 3.616f, 11.512f, 3.356f, 10.873f)
                curveTo(3.096f, 10.233f, 2.971f, 9.547f, 2.989f, 8.857f)
                curveTo(3.007f, 8.167f, 3.168f, 7.488f, 3.461f, 6.863f)
                curveTo(3.755f, 6.239f, 4.174f, 5.681f, 4.694f, 5.227f)
                curveTo(5.213f, 4.772f, 5.821f, 4.43f, 6.479f, 4.221f)
                curveTo(7.137f, 4.013f, 7.831f, 3.944f, 8.517f, 4.017f)
                curveTo(9.204f, 4.09f, 9.868f, 4.305f, 10.467f, 4.647f)
                curveTo(11.066f, 4.989f, 11.588f, 5.452f, 12f, 6.006f)
                curveTo(12.414f, 5.456f, 12.936f, 4.997f, 13.535f, 4.659f)
                curveTo(14.134f, 4.32f, 14.797f, 4.108f, 15.481f, 4.038f)
                curveTo(16.165f, 3.967f, 16.857f, 4.038f, 17.513f, 4.246f)
                curveTo(18.169f, 4.455f, 18.774f, 4.797f, 19.292f, 5.25f)
                curveTo(19.809f, 5.704f, 20.228f, 6.259f, 20.521f, 6.882f)
                curveTo(20.813f, 7.504f, 20.975f, 8.181f, 20.994f, 8.869f)
                curveTo(21.014f, 9.557f, 20.891f, 10.241f, 20.634f, 10.879f)
                curveTo(20.377f, 11.517f, 19.991f, 12.096f, 19.5f, 12.578f)
            }
        }.build()

        return _Hearth!!
    }

@Suppress("ObjectPropertyName")
private var _Hearth: ImageVector? = null
