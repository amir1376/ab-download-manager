package com.abdownloadmanager.desktop.ui.icons.default

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Default.Speaker: ImageVector
    get() {
        if (_Speaker != null) {
            return _Speaker!!
        }
        _Speaker = ImageVector.Builder(
            name = "Default.Speaker",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(11.94f, 16.139f)
                lineTo(16.839f, 18.968f)
                lineTo(18.771f, 18.45f)
                lineTo(14.63f, 2.996f)
                lineTo(12.698f, 3.513f)
                lineTo(9.869f, 8.412f)
                lineTo(3.108f, 10.224f)
                curveTo(2.045f, 10.509f, 1.409f, 11.611f, 1.694f, 12.673f)
                lineTo(2.729f, 16.537f)
                curveTo(3.014f, 17.6f, 4.116f, 18.236f, 5.178f, 17.951f)
                lineTo(6.144f, 17.692f)
                lineTo(6.921f, 20.59f)
                curveTo(7.206f, 21.653f, 8.308f, 22.289f, 9.37f, 22.004f)
                lineTo(11.302f, 21.487f)
                lineTo(10.008f, 16.657f)
                lineTo(11.94f, 16.139f)
                close()
                moveTo(9.832f, 20.638f)
                lineTo(8.538f, 15.809f)
                lineTo(12.109f, 14.852f)
                lineTo(17.008f, 17.68f)
                lineTo(17.301f, 17.602f)
                lineTo(13.781f, 4.465f)
                lineTo(13.488f, 4.544f)
                lineTo(10.66f, 9.443f)
                lineTo(3.418f, 11.383f)
                curveTo(2.996f, 11.496f, 2.74f, 11.94f, 2.853f, 12.363f)
                lineTo(3.888f, 16.226f)
                curveTo(4.001f, 16.649f, 4.445f, 16.905f, 4.868f, 16.792f)
                lineTo(6.993f, 16.223f)
                lineTo(8.08f, 20.28f)
                curveTo(8.193f, 20.702f, 8.637f, 20.958f, 9.06f, 20.845f)
                lineTo(9.832f, 20.638f)
                close()
                moveTo(20.507f, 12.565f)
                curveTo(20.194f, 12.806f, 19.824f, 12.992f, 19.409f, 13.103f)
                lineTo(17.856f, 7.308f)
                curveTo(18.271f, 7.196f, 18.684f, 7.172f, 19.076f, 7.224f)
                curveTo(20.23f, 7.379f, 21.201f, 8.203f, 21.53f, 9.429f)
                curveTo(21.858f, 10.655f, 21.429f, 11.853f, 20.507f, 12.565f)
                close()
                moveTo(19.436f, 8.57f)
                lineTo(20.146f, 11.22f)
                curveTo(20.424f, 10.811f, 20.517f, 10.285f, 20.371f, 9.739f)
                curveTo(20.225f, 9.194f, 19.881f, 8.785f, 19.436f, 8.57f)
                close()
            }
        }.build()

        return _Speaker!!
    }

@Suppress("ObjectPropertyName")
private var _Speaker: ImageVector? = null
