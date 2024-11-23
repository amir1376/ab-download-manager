package com.abdownloadmanager.desktop.ui.icons.colored

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons

val AbIcons.Colored.AppIcon: ImageVector
    get() {
        if (_AppIcon != null) {
            return _AppIcon!!
        }
        _AppIcon = ImageVector.Builder(
            name = "Colored.AppIcon",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFC631FF),
                        1f to Color(0xFF4DC4FE)
                    ),
                    start = Offset(50.062f, 23.014f),
                    end = Offset(-1.681f, 23.014f)
                ),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(26.89f, 0.892f)
                curveTo(26.89f, 0.399f, 26.51f, 0f, 26.04f, 0f)
                horizontalLineTo(21.96f)
                curveTo(21.49f, 0f, 21.11f, 0.399f, 21.11f, 0.892f)
                verticalLineTo(17.84f)
                curveTo(21.11f, 18.333f, 20.729f, 18.732f, 20.26f, 18.732f)
                horizontalLineTo(18.851f)
                curveTo(18.16f, 18.732f, 17.758f, 19.552f, 18.16f, 20.143f)
                lineTo(23.308f, 27.706f)
                curveTo(23.647f, 28.205f, 24.353f, 28.205f, 24.692f, 27.706f)
                lineTo(29.84f, 20.143f)
                curveTo(30.242f, 19.552f, 29.84f, 18.732f, 29.149f, 18.732f)
                horizontalLineTo(27.74f)
                curveTo(27.271f, 18.732f, 26.89f, 18.333f, 26.89f, 17.84f)
                verticalLineTo(0.892f)
                close()
                moveTo(1.827f, 33.184f)
                curveTo(0.621f, 30.273f, 0f, 27.152f, 0f, 24f)
                horizontalLineTo(7.201f)
                curveTo(9.804f, 24f, 11.831f, 26.188f, 12.827f, 28.592f)
                curveTo(13.43f, 30.048f, 14.314f, 31.371f, 15.428f, 32.485f)
                curveTo(16.543f, 33.6f, 17.866f, 34.484f, 19.322f, 35.087f)
                curveTo(20.777f, 35.69f, 22.338f, 36f, 23.914f, 36f)
                curveTo(25.49f, 36f, 27.05f, 35.69f, 28.506f, 35.087f)
                curveTo(29.962f, 34.484f, 31.285f, 33.6f, 32.399f, 32.485f)
                curveTo(33.513f, 31.371f, 34.397f, 30.048f, 35f, 28.592f)
                curveTo(35.996f, 26.188f, 38.023f, 24f, 40.626f, 24f)
                horizontalLineTo(48f)
                curveTo(48f, 27.152f, 47.379f, 30.273f, 46.173f, 33.184f)
                curveTo(44.967f, 36.096f, 43.199f, 38.742f, 40.971f, 40.971f)
                curveTo(38.742f, 43.199f, 36.096f, 44.967f, 33.184f, 46.173f)
                curveTo(30.273f, 47.379f, 27.152f, 48f, 24f, 48f)
                curveTo(20.848f, 48f, 17.727f, 47.379f, 14.816f, 46.173f)
                curveTo(11.904f, 44.967f, 9.258f, 43.199f, 7.029f, 40.971f)
                curveTo(4.801f, 38.742f, 3.033f, 36.096f, 1.827f, 33.184f)
                close()
                moveTo(11.772f, 5.211f)
                curveTo(11.126f, 5.731f, 11.016f, 6.686f, 11.525f, 7.345f)
                curveTo(12.035f, 8.003f, 12.971f, 8.116f, 13.617f, 7.596f)
                lineTo(15.808f, 5.832f)
                curveTo(16.454f, 5.312f, 16.564f, 4.357f, 16.055f, 3.698f)
                curveTo(15.545f, 3.039f, 14.609f, 2.927f, 13.963f, 3.447f)
                lineTo(11.772f, 5.211f)
                close()
                moveTo(36.468f, 5.211f)
                curveTo(37.114f, 5.731f, 37.224f, 6.686f, 36.715f, 7.345f)
                curveTo(36.205f, 8.003f, 35.269f, 8.116f, 34.623f, 7.596f)
                lineTo(32.432f, 5.832f)
                curveTo(31.786f, 5.312f, 31.676f, 4.357f, 32.185f, 3.698f)
                curveTo(32.695f, 3.039f, 33.631f, 2.927f, 34.277f, 3.447f)
                lineTo(36.468f, 5.211f)
                close()
                moveTo(4.543f, 17.654f)
                curveTo(3.778f, 17.346f, 3.403f, 16.464f, 3.704f, 15.683f)
                lineTo(4.728f, 13.033f)
                curveTo(5.03f, 12.253f, 5.895f, 11.87f, 6.66f, 12.177f)
                curveTo(7.425f, 12.485f, 7.8f, 13.368f, 7.499f, 14.148f)
                lineTo(6.475f, 16.798f)
                curveTo(6.173f, 17.578f, 5.308f, 17.962f, 4.543f, 17.654f)
                close()
                moveTo(44.536f, 15.683f)
                curveTo(44.838f, 16.464f, 44.462f, 17.346f, 43.697f, 17.654f)
                curveTo(42.932f, 17.962f, 42.067f, 17.578f, 41.765f, 16.798f)
                lineTo(40.741f, 14.148f)
                curveTo(40.44f, 13.368f, 40.815f, 12.485f, 41.58f, 12.177f)
                curveTo(42.345f, 11.87f, 43.21f, 12.253f, 43.512f, 13.033f)
                lineTo(44.536f, 15.683f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.25f
            ) {
                moveTo(40.97f, 40.97f)
                curveTo(36.47f, 45.471f, 30.365f, 48f, 24f, 48f)
                curveTo(17.635f, 48f, 11.53f, 45.471f, 7.029f, 40.97f)
                verticalLineTo(40.97f)
                curveTo(9.558f, 38.441f, 13.649f, 38.586f, 16.921f, 40.03f)
                curveTo(19.13f, 41.006f, 21.538f, 41.524f, 24f, 41.524f)
                curveTo(26.462f, 41.524f, 28.87f, 41.006f, 31.079f, 40.03f)
                curveTo(34.351f, 38.586f, 38.441f, 38.441f, 40.97f, 40.97f)
                verticalLineTo(40.97f)
                close()
            }
        }.build()

        return _AppIcon!!
    }

@Suppress("ObjectPropertyName")
private var _AppIcon: ImageVector? = null
