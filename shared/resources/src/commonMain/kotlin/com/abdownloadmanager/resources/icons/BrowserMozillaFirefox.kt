package com.abdownloadmanager.resources.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.BrowserMozillaFirefox: ImageVector
    get() {
        if (_BrowserMozillaFirefox != null) {
            return _BrowserMozillaFirefox!!
        }
        _BrowserMozillaFirefox = ImageVector.Builder(
            name = "BrowserMozillaFirefox",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(24f)
                    verticalLineToRelative(24f)
                    horizontalLineToRelative(-24f)
                    close()
                }
            ) {
                path(
                    fill = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.048f to Color(0xFFFFF44F),
                            0.111f to Color(0xFFFFE847),
                            0.225f to Color(0xFFFFC830),
                            0.368f to Color(0xFFFF980E),
                            0.401f to Color(0xFFFF8B16),
                            0.462f to Color(0xFFFF672A),
                            0.534f to Color(0xFFFF3647),
                            0.705f to Color(0xFFE31587)
                        ),
                        start = Offset(21.172f, 3.71f),
                        end = Offset(1.904f, 22.3f)
                    )
                ) {
                    moveTo(22.708f, 8.034f)
                    curveTo(22.204f, 6.82f, 21.181f, 5.51f, 20.379f, 5.096f)
                    curveTo(20.951f, 6.203f, 21.347f, 7.391f, 21.555f, 8.619f)
                    lineTo(21.557f, 8.639f)
                    curveTo(20.245f, 5.367f, 18.019f, 4.048f, 16.202f, 1.175f)
                    curveTo(16.108f, 1.029f, 16.017f, 0.88f, 15.928f, 0.731f)
                    curveTo(15.883f, 0.652f, 15.84f, 0.572f, 15.801f, 0.491f)
                    curveTo(15.725f, 0.345f, 15.667f, 0.191f, 15.627f, 0.031f)
                    curveTo(15.627f, 0.024f, 15.625f, 0.017f, 15.62f, 0.011f)
                    curveTo(15.615f, 0.006f, 15.608f, 0.002f, 15.601f, 0.001f)
                    curveTo(15.594f, -0f, 15.586f, -0f, 15.579f, 0.001f)
                    curveTo(15.578f, 0.001f, 15.575f, 0.004f, 15.573f, 0.005f)
                    curveTo(15.572f, 0.005f, 15.568f, 0.008f, 15.565f, 0.009f)
                    lineTo(15.57f, 0.001f)
                    curveTo(12.654f, 1.708f, 11.665f, 4.868f, 11.574f, 6.449f)
                    curveTo(10.41f, 6.529f, 9.297f, 6.958f, 8.38f, 7.68f)
                    curveTo(8.284f, 7.599f, 8.184f, 7.523f, 8.08f, 7.453f)
                    curveTo(7.816f, 6.528f, 7.805f, 5.548f, 8.048f, 4.616f)
                    curveTo(6.977f, 5.135f, 6.026f, 5.87f, 5.254f, 6.775f)
                    horizontalLineTo(5.249f)
                    curveTo(4.789f, 6.192f, 4.821f, 4.27f, 4.847f, 3.868f)
                    curveTo(4.711f, 3.923f, 4.581f, 3.992f, 4.46f, 4.074f)
                    curveTo(4.054f, 4.364f, 3.674f, 4.689f, 3.325f, 5.046f)
                    curveTo(2.928f, 5.449f, 2.565f, 5.884f, 2.24f, 6.348f)
                    verticalLineTo(6.349f)
                    verticalLineTo(6.347f)
                    curveTo(1.494f, 7.405f, 0.965f, 8.6f, 0.683f, 9.864f)
                    lineTo(0.668f, 9.941f)
                    curveTo(0.625f, 10.181f, 0.587f, 10.423f, 0.553f, 10.665f)
                    curveTo(0.553f, 10.674f, 0.552f, 10.682f, 0.551f, 10.691f)
                    curveTo(0.449f, 11.219f, 0.386f, 11.754f, 0.362f, 12.291f)
                    verticalLineTo(12.351f)
                    curveTo(0.37f, 21.286f, 10.048f, 26.862f, 17.782f, 22.388f)
                    curveTo(19.254f, 21.536f, 20.521f, 20.372f, 21.493f, 18.976f)
                    curveTo(22.465f, 17.581f, 23.119f, 15.989f, 23.408f, 14.314f)
                    curveTo(23.428f, 14.164f, 23.443f, 14.016f, 23.461f, 13.864f)
                    curveTo(23.7f, 11.889f, 23.441f, 9.884f, 22.708f, 8.034f)
                    close()
                    moveTo(9.33f, 17.119f)
                    curveTo(9.385f, 17.145f, 9.435f, 17.174f, 9.491f, 17.198f)
                    lineTo(9.499f, 17.204f)
                    curveTo(9.443f, 17.176f, 9.386f, 17.148f, 9.33f, 17.119f)
                    close()
                    moveTo(21.558f, 8.641f)
                    verticalLineTo(8.63f)
                    lineTo(21.56f, 8.642f)
                    lineTo(21.558f, 8.641f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.129f to Color(0xFFFFBD4F),
                            0.186f to Color(0xFFFFAC31),
                            0.247f to Color(0xFFFF9D17),
                            0.283f to Color(0xFFFF980E),
                            0.403f to Color(0xFFFF563B),
                            0.467f to Color(0xFFFF3750),
                            0.71f to Color(0xFFF5156C),
                            0.782f to Color(0xFFEB0878),
                            0.86f to Color(0xFFE50080)
                        ),
                        center = Offset(20.282f, 2.658f),
                        radius = 24.197f
                    )
                ) {
                    moveTo(22.708f, 8.034f)
                    curveTo(22.204f, 6.82f, 21.181f, 5.51f, 20.379f, 5.096f)
                    curveTo(20.951f, 6.203f, 21.347f, 7.391f, 21.555f, 8.619f)
                    verticalLineTo(8.63f)
                    lineTo(21.557f, 8.642f)
                    curveTo(22.452f, 11.203f, 22.322f, 14.009f, 21.196f, 16.476f)
                    curveTo(19.866f, 19.33f, 16.646f, 22.256f, 11.605f, 22.114f)
                    curveTo(6.16f, 21.959f, 1.363f, 17.918f, 0.467f, 12.625f)
                    curveTo(0.304f, 11.791f, 0.467f, 11.368f, 0.549f, 10.689f)
                    curveTo(0.437f, 11.216f, 0.374f, 11.752f, 0.362f, 12.291f)
                    verticalLineTo(12.351f)
                    curveTo(0.37f, 21.286f, 10.048f, 26.862f, 17.782f, 22.388f)
                    curveTo(19.254f, 21.536f, 20.521f, 20.372f, 21.493f, 18.976f)
                    curveTo(22.465f, 17.581f, 23.119f, 15.989f, 23.408f, 14.314f)
                    curveTo(23.428f, 14.164f, 23.443f, 14.016f, 23.461f, 13.864f)
                    curveTo(23.7f, 11.889f, 23.441f, 9.884f, 22.708f, 8.034f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.3f to Color(0xFF960E18),
                            0.351f to Color(0xBCB11927),
                            0.435f to Color(0x57DB293D),
                            0.497f to Color(0x17F5334B),
                            0.53f to Color(0x00FF3750)
                        ),
                        center = Offset(11.44f, 12.55f),
                        radius = 24.197f
                    )
                ) {
                    moveTo(22.708f, 8.034f)
                    curveTo(22.204f, 6.82f, 21.181f, 5.51f, 20.379f, 5.096f)
                    curveTo(20.951f, 6.203f, 21.347f, 7.391f, 21.555f, 8.619f)
                    verticalLineTo(8.63f)
                    lineTo(21.557f, 8.642f)
                    curveTo(22.452f, 11.203f, 22.322f, 14.009f, 21.196f, 16.476f)
                    curveTo(19.866f, 19.33f, 16.646f, 22.256f, 11.605f, 22.114f)
                    curveTo(6.16f, 21.959f, 1.363f, 17.918f, 0.467f, 12.625f)
                    curveTo(0.304f, 11.791f, 0.467f, 11.368f, 0.549f, 10.689f)
                    curveTo(0.437f, 11.216f, 0.374f, 11.752f, 0.362f, 12.291f)
                    verticalLineTo(12.351f)
                    curveTo(0.37f, 21.286f, 10.048f, 26.862f, 17.782f, 22.388f)
                    curveTo(19.254f, 21.536f, 20.521f, 20.372f, 21.493f, 18.976f)
                    curveTo(22.465f, 17.581f, 23.119f, 15.989f, 23.408f, 14.314f)
                    curveTo(23.428f, 14.164f, 23.443f, 14.016f, 23.461f, 13.864f)
                    curveTo(23.7f, 11.889f, 23.441f, 9.884f, 22.708f, 8.034f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.132f to Color(0xFFFFF44F),
                            0.252f to Color(0xFFFFDC3E),
                            0.506f to Color(0xFFFF9D12),
                            0.526f to Color(0xFFFF980E)
                        ),
                        center = Offset(14.357f, -2.833f),
                        radius = 17.529f
                    )
                ) {
                    moveTo(17.067f, 9.398f)
                    curveTo(17.093f, 9.416f, 17.116f, 9.434f, 17.14f, 9.451f)
                    curveTo(16.848f, 8.934f, 16.485f, 8.461f, 16.062f, 8.045f)
                    curveTo(12.454f, 4.437f, 15.116f, 0.222f, 15.565f, 0.008f)
                    lineTo(15.57f, 0.001f)
                    curveTo(12.654f, 1.708f, 11.665f, 4.868f, 11.574f, 6.449f)
                    curveTo(11.709f, 6.44f, 11.844f, 6.428f, 11.982f, 6.428f)
                    curveTo(13.016f, 6.43f, 14.032f, 6.706f, 14.925f, 7.228f)
                    curveTo(15.818f, 7.749f, 16.558f, 8.498f, 17.067f, 9.398f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.353f to Color(0xFF3A8EE6),
                            0.472f to Color(0xFF5C79F0),
                            0.669f to Color(0xFF9059FF),
                            1f to Color(0xFFC139E6)
                        ),
                        center = Offset(8.763f, 18.871f),
                        radius = 11.521f
                    )
                ) {
                    moveTo(11.989f, 10.119f)
                    curveTo(11.97f, 10.408f, 10.95f, 11.403f, 10.594f, 11.403f)
                    curveTo(7.293f, 11.403f, 6.757f, 13.4f, 6.757f, 13.4f)
                    curveTo(6.903f, 15.081f, 8.075f, 16.466f, 9.491f, 17.198f)
                    curveTo(9.556f, 17.232f, 9.621f, 17.262f, 9.687f, 17.292f)
                    curveTo(9.799f, 17.342f, 9.913f, 17.388f, 10.028f, 17.431f)
                    curveTo(10.514f, 17.603f, 11.023f, 17.702f, 11.538f, 17.723f)
                    curveTo(17.323f, 17.994f, 18.444f, 10.805f, 14.269f, 8.719f)
                    curveTo(15.254f, 8.591f, 16.251f, 8.833f, 17.067f, 9.398f)
                    curveTo(16.558f, 8.498f, 15.818f, 7.749f, 14.925f, 7.228f)
                    curveTo(14.032f, 6.706f, 13.016f, 6.43f, 11.982f, 6.428f)
                    curveTo(11.844f, 6.428f, 11.709f, 6.44f, 11.574f, 6.449f)
                    curveTo(10.41f, 6.529f, 9.297f, 6.958f, 8.38f, 7.68f)
                    curveTo(8.557f, 7.83f, 8.757f, 8.03f, 9.177f, 8.445f)
                    curveTo(9.965f, 9.221f, 11.985f, 10.024f, 11.989f, 10.119f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.206f to Color(0x009059FF),
                            0.278f to Color(0x108C4FF3),
                            0.747f to Color(0x727716A8),
                            0.975f to Color(0x996E008B)
                        ),
                        center = Offset(12.766f, 10.568f),
                        radius = 6.108f
                    )
                ) {
                    moveTo(11.989f, 10.119f)
                    curveTo(11.97f, 10.408f, 10.95f, 11.403f, 10.594f, 11.403f)
                    curveTo(7.293f, 11.403f, 6.757f, 13.4f, 6.757f, 13.4f)
                    curveTo(6.903f, 15.081f, 8.075f, 16.466f, 9.491f, 17.198f)
                    curveTo(9.556f, 17.232f, 9.621f, 17.262f, 9.687f, 17.292f)
                    curveTo(9.799f, 17.342f, 9.913f, 17.388f, 10.028f, 17.431f)
                    curveTo(10.514f, 17.603f, 11.023f, 17.702f, 11.538f, 17.723f)
                    curveTo(17.323f, 17.994f, 18.444f, 10.805f, 14.269f, 8.719f)
                    curveTo(15.254f, 8.591f, 16.251f, 8.833f, 17.067f, 9.398f)
                    curveTo(16.558f, 8.498f, 15.818f, 7.749f, 14.925f, 7.228f)
                    curveTo(14.032f, 6.706f, 13.016f, 6.43f, 11.982f, 6.428f)
                    curveTo(11.844f, 6.428f, 11.709f, 6.44f, 11.574f, 6.449f)
                    curveTo(10.41f, 6.529f, 9.297f, 6.958f, 8.38f, 7.68f)
                    curveTo(8.557f, 7.83f, 8.757f, 8.03f, 9.177f, 8.445f)
                    curveTo(9.965f, 9.221f, 11.985f, 10.024f, 11.989f, 10.119f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFFFE226),
                            0.121f to Color(0xFFFFDB27),
                            0.295f to Color(0xFFFFC82A),
                            0.502f to Color(0xFFFFA930),
                            0.732f to Color(0xFFFF7E37),
                            0.792f to Color(0xFFFF7139)
                        ),
                        center = Offset(11.134f, 1.668f),
                        radius = 8.288f
                    )
                ) {
                    moveTo(7.839f, 7.294f)
                    curveTo(7.92f, 7.346f, 7.999f, 7.399f, 8.078f, 7.453f)
                    curveTo(7.814f, 6.528f, 7.802f, 5.548f, 8.046f, 4.616f)
                    curveTo(6.975f, 5.135f, 6.024f, 5.87f, 5.252f, 6.776f)
                    curveTo(5.308f, 6.774f, 6.992f, 6.744f, 7.839f, 7.294f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.113f to Color(0xFFFFF44F),
                            0.456f to Color(0xFFFF980E),
                            0.622f to Color(0xFFFF5634),
                            0.716f to Color(0xFFFF3647),
                            0.904f to Color(0xFFE31587)
                        ),
                        center = Offset(17.649f, -3.589f),
                        radius = 35.362f
                    )
                ) {
                    moveTo(0.468f, 12.625f)
                    curveTo(1.364f, 17.918f, 6.161f, 21.959f, 11.607f, 22.114f)
                    curveTo(16.647f, 22.256f, 19.867f, 19.33f, 21.197f, 16.476f)
                    curveTo(22.324f, 14.009f, 22.453f, 11.203f, 21.559f, 8.642f)
                    verticalLineTo(8.631f)
                    curveTo(21.559f, 8.623f, 21.557f, 8.618f, 21.559f, 8.62f)
                    lineTo(21.561f, 8.64f)
                    curveTo(21.972f, 11.328f, 20.605f, 13.933f, 18.467f, 15.694f)
                    lineTo(18.461f, 15.709f)
                    curveTo(14.296f, 19.101f, 10.31f, 17.755f, 9.503f, 17.206f)
                    curveTo(9.446f, 17.179f, 9.39f, 17.151f, 9.334f, 17.122f)
                    curveTo(6.905f, 15.961f, 5.902f, 13.749f, 6.117f, 11.851f)
                    curveTo(5.541f, 11.86f, 4.974f, 11.701f, 4.486f, 11.394f)
                    curveTo(3.998f, 11.087f, 3.61f, 10.645f, 3.368f, 10.122f)
                    curveTo(4.005f, 9.731f, 4.732f, 9.511f, 5.479f, 9.481f)
                    curveTo(6.226f, 9.451f, 6.968f, 9.612f, 7.635f, 9.951f)
                    curveTo(9.01f, 10.575f, 10.574f, 10.636f, 11.993f, 10.122f)
                    curveTo(11.988f, 10.027f, 9.969f, 9.223f, 9.181f, 8.448f)
                    curveTo(8.76f, 8.033f, 8.56f, 7.833f, 8.383f, 7.683f)
                    curveTo(8.288f, 7.602f, 8.188f, 7.526f, 8.084f, 7.456f)
                    curveTo(8.015f, 7.409f, 7.937f, 7.358f, 7.844f, 7.297f)
                    curveTo(6.998f, 6.747f, 5.314f, 6.777f, 5.258f, 6.778f)
                    horizontalLineTo(5.253f)
                    curveTo(4.793f, 6.195f, 4.825f, 4.273f, 4.852f, 3.871f)
                    curveTo(4.716f, 3.926f, 4.586f, 3.995f, 4.464f, 4.077f)
                    curveTo(4.058f, 4.367f, 3.678f, 4.692f, 3.33f, 5.049f)
                    curveTo(2.931f, 5.45f, 2.567f, 5.885f, 2.24f, 6.348f)
                    verticalLineTo(6.349f)
                    verticalLineTo(6.347f)
                    curveTo(1.494f, 7.405f, 0.965f, 8.6f, 0.683f, 9.864f)
                    curveTo(0.677f, 9.888f, 0.265f, 11.69f, 0.468f, 12.625f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFFFF44F),
                            0.06f to Color(0xFFFFE847),
                            0.168f to Color(0xFFFFC830),
                            0.304f to Color(0xFFFF980E),
                            0.356f to Color(0xFFFF8B16),
                            0.455f to Color(0xFFFF672A),
                            0.57f to Color(0xFFFF3647),
                            0.737f to Color(0xFFE31587)
                        ),
                        center = Offset(14.674f, -1.623f),
                        radius = 25.918f
                    )
                ) {
                    moveTo(16.062f, 8.045f)
                    curveTo(16.486f, 8.461f, 16.849f, 8.935f, 17.14f, 9.453f)
                    curveTo(17.201f, 9.498f, 17.258f, 9.545f, 17.314f, 9.595f)
                    curveTo(19.946f, 12.021f, 18.567f, 15.45f, 18.464f, 15.694f)
                    curveTo(20.602f, 13.933f, 21.968f, 11.328f, 21.558f, 8.64f)
                    curveTo(20.245f, 5.367f, 18.019f, 4.048f, 16.202f, 1.175f)
                    curveTo(16.108f, 1.029f, 16.017f, 0.88f, 15.928f, 0.731f)
                    curveTo(15.883f, 0.652f, 15.84f, 0.572f, 15.8f, 0.491f)
                    curveTo(15.725f, 0.345f, 15.667f, 0.191f, 15.627f, 0.031f)
                    curveTo(15.627f, 0.024f, 15.625f, 0.017f, 15.62f, 0.011f)
                    curveTo(15.615f, 0.006f, 15.608f, 0.002f, 15.601f, 0.001f)
                    curveTo(15.594f, -0f, 15.586f, -0f, 15.579f, 0.001f)
                    curveTo(15.578f, 0.001f, 15.575f, 0.004f, 15.573f, 0.005f)
                    curveTo(15.572f, 0.005f, 15.568f, 0.008f, 15.565f, 0.009f)
                    curveTo(15.116f, 0.222f, 12.454f, 4.437f, 16.062f, 8.045f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.137f to Color(0xFFFFF44F),
                            0.48f to Color(0xFFFF980E),
                            0.592f to Color(0xFFFF5634),
                            0.655f to Color(0xFFFF3647),
                            0.904f to Color(0xFFE31587)
                        ),
                        center = Offset(10.939f, 4.738f),
                        radius = 22.077f
                    )
                ) {
                    moveTo(17.313f, 9.594f)
                    curveTo(17.257f, 9.544f, 17.199f, 9.496f, 17.139f, 9.451f)
                    curveTo(17.115f, 9.434f, 17.092f, 9.416f, 17.066f, 9.398f)
                    curveTo(16.25f, 8.833f, 15.253f, 8.591f, 14.268f, 8.719f)
                    curveTo(18.442f, 10.806f, 17.322f, 17.994f, 11.537f, 17.723f)
                    curveTo(11.022f, 17.702f, 10.513f, 17.603f, 10.027f, 17.431f)
                    curveTo(9.912f, 17.388f, 9.798f, 17.342f, 9.686f, 17.292f)
                    curveTo(9.62f, 17.262f, 9.555f, 17.232f, 9.49f, 17.198f)
                    lineTo(9.498f, 17.204f)
                    curveTo(10.305f, 17.754f, 14.29f, 19.1f, 18.456f, 15.706f)
                    lineTo(18.462f, 15.691f)
                    curveTo(18.566f, 15.448f, 19.945f, 12.019f, 17.313f, 9.594f)
                    close()
                }
                path(
                    fill = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.094f to Color(0xFFFFF44F),
                            0.231f to Color(0xFFFFE141),
                            0.509f to Color(0xFFFFAF1E),
                            0.626f to Color(0xFFFF980E)
                        ),
                        center = Offset(16.767f, 6.03f),
                        radius = 24.163f
                    )
                ) {
                    moveTo(6.757f, 13.4f)
                    curveTo(6.757f, 13.4f, 7.293f, 11.403f, 10.594f, 11.403f)
                    curveTo(10.95f, 11.403f, 11.971f, 10.408f, 11.989f, 10.119f)
                    curveTo(10.57f, 10.633f, 9.006f, 10.571f, 7.631f, 9.947f)
                    curveTo(6.965f, 9.609f, 6.222f, 9.448f, 5.475f, 9.478f)
                    curveTo(4.728f, 9.508f, 4.002f, 9.728f, 3.364f, 10.119f)
                    curveTo(3.606f, 10.642f, 3.995f, 11.084f, 4.483f, 11.391f)
                    curveTo(4.971f, 11.698f, 5.537f, 11.857f, 6.114f, 11.848f)
                    curveTo(5.899f, 13.746f, 6.902f, 15.959f, 9.33f, 17.119f)
                    curveTo(9.385f, 17.145f, 9.435f, 17.173f, 9.491f, 17.198f)
                    curveTo(8.074f, 16.466f, 6.903f, 15.081f, 6.757f, 13.4f)
                    close()
                }
                path(
                    fill = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.167f to Color(0xCCFFF44F),
                            0.266f to Color(0xA1FFF44F),
                            0.489f to Color(0x37FFF44F),
                            0.6f to Color(0x00FFF44F)
                        ),
                        start = Offset(20.94f, 3.611f),
                        end = Offset(4.545f, 20.005f)
                    )
                ) {
                    moveTo(22.708f, 8.034f)
                    curveTo(22.204f, 6.82f, 21.181f, 5.51f, 20.379f, 5.096f)
                    curveTo(20.951f, 6.203f, 21.347f, 7.391f, 21.555f, 8.619f)
                    lineTo(21.557f, 8.639f)
                    curveTo(20.245f, 5.367f, 18.019f, 4.048f, 16.202f, 1.175f)
                    curveTo(16.108f, 1.029f, 16.017f, 0.88f, 15.928f, 0.731f)
                    curveTo(15.883f, 0.652f, 15.84f, 0.572f, 15.801f, 0.491f)
                    curveTo(15.725f, 0.345f, 15.667f, 0.191f, 15.627f, 0.031f)
                    curveTo(15.627f, 0.024f, 15.625f, 0.017f, 15.62f, 0.011f)
                    curveTo(15.615f, 0.006f, 15.608f, 0.002f, 15.601f, 0.001f)
                    curveTo(15.594f, -0f, 15.586f, -0f, 15.579f, 0.001f)
                    curveTo(15.578f, 0.001f, 15.575f, 0.004f, 15.573f, 0.005f)
                    curveTo(15.572f, 0.005f, 15.568f, 0.008f, 15.565f, 0.009f)
                    lineTo(15.57f, 0.001f)
                    curveTo(12.654f, 1.708f, 11.665f, 4.868f, 11.574f, 6.449f)
                    curveTo(11.709f, 6.44f, 11.844f, 6.428f, 11.982f, 6.428f)
                    curveTo(13.016f, 6.43f, 14.032f, 6.706f, 14.925f, 7.228f)
                    curveTo(15.818f, 7.749f, 16.558f, 8.498f, 17.068f, 9.398f)
                    curveTo(16.251f, 8.833f, 15.254f, 8.591f, 14.269f, 8.719f)
                    curveTo(18.444f, 10.806f, 17.324f, 17.994f, 11.538f, 17.723f)
                    curveTo(11.023f, 17.702f, 10.514f, 17.603f, 10.028f, 17.431f)
                    curveTo(9.913f, 17.388f, 9.799f, 17.342f, 9.687f, 17.292f)
                    curveTo(9.622f, 17.262f, 9.556f, 17.232f, 9.491f, 17.198f)
                    lineTo(9.499f, 17.204f)
                    curveTo(9.443f, 17.176f, 9.386f, 17.148f, 9.33f, 17.119f)
                    curveTo(9.385f, 17.145f, 9.435f, 17.174f, 9.491f, 17.198f)
                    curveTo(8.074f, 16.466f, 6.903f, 15.081f, 6.757f, 13.4f)
                    curveTo(6.757f, 13.4f, 7.293f, 11.403f, 10.594f, 11.403f)
                    curveTo(10.95f, 11.403f, 11.971f, 10.408f, 11.989f, 10.119f)
                    curveTo(11.985f, 10.024f, 9.965f, 9.22f, 9.177f, 8.445f)
                    curveTo(8.757f, 8.03f, 8.557f, 7.83f, 8.38f, 7.68f)
                    curveTo(8.284f, 7.599f, 8.184f, 7.523f, 8.08f, 7.453f)
                    curveTo(7.816f, 6.528f, 7.805f, 5.548f, 8.048f, 4.616f)
                    curveTo(6.977f, 5.135f, 6.026f, 5.87f, 5.254f, 6.775f)
                    horizontalLineTo(5.249f)
                    curveTo(4.789f, 6.192f, 4.821f, 4.27f, 4.847f, 3.868f)
                    curveTo(4.711f, 3.923f, 4.581f, 3.992f, 4.46f, 4.074f)
                    curveTo(4.054f, 4.364f, 3.674f, 4.689f, 3.325f, 5.046f)
                    curveTo(2.928f, 5.449f, 2.565f, 5.884f, 2.24f, 6.348f)
                    verticalLineTo(6.349f)
                    verticalLineTo(6.347f)
                    curveTo(1.494f, 7.405f, 0.965f, 8.6f, 0.683f, 9.864f)
                    lineTo(0.668f, 9.941f)
                    curveTo(0.646f, 10.043f, 0.548f, 10.561f, 0.534f, 10.673f)
                    curveTo(0.534f, 10.681f, 0.534f, 10.664f, 0.534f, 10.673f)
                    curveTo(0.444f, 11.208f, 0.387f, 11.749f, 0.362f, 12.291f)
                    verticalLineTo(12.351f)
                    curveTo(0.37f, 21.286f, 10.048f, 26.862f, 17.782f, 22.388f)
                    curveTo(19.254f, 21.536f, 20.521f, 20.372f, 21.493f, 18.976f)
                    curveTo(22.465f, 17.581f, 23.119f, 15.989f, 23.408f, 14.314f)
                    curveTo(23.428f, 14.164f, 23.443f, 14.016f, 23.461f, 13.864f)
                    curveTo(23.7f, 11.889f, 23.441f, 9.884f, 22.708f, 8.034f)
                    close()
                }
            }
        }.build()

        return _BrowserMozillaFirefox!!
    }

@Suppress("ObjectPropertyName")
private var _BrowserMozillaFirefox: ImageVector? = null
