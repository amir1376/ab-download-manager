package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.QuestionMark: ImageVector
    get() {
        if (_QuestionMark != null) {
            return _QuestionMark!!
        }
        _QuestionMark = ImageVector.Builder(
            name = "QuestionMark",
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
                moveTo(8f, 8f)
                curveTo(8f, 7.204f, 8.369f, 6.441f, 9.025f, 5.879f)
                curveTo(9.682f, 5.316f, 10.572f, 5f, 11.5f, 5f)
                horizontalLineTo(12.5f)
                curveTo(13.428f, 5f, 14.318f, 5.316f, 14.975f, 5.879f)
                curveTo(15.631f, 6.441f, 16f, 7.204f, 16f, 8f)
                curveTo(16.037f, 8.649f, 15.862f, 9.293f, 15.501f, 9.834f)
                curveTo(15.14f, 10.375f, 14.613f, 10.784f, 14f, 11f)
                curveTo(13.387f, 11.288f, 12.86f, 11.833f, 12.499f, 12.555f)
                curveTo(12.138f, 13.276f, 11.963f, 14.134f, 12f, 15f)
                moveTo(12f, 19f)
                verticalLineTo(19.01f)
            }
        }.build()

        return _QuestionMark!!
    }

@Suppress("ObjectPropertyName")
private var _QuestionMark: ImageVector? = null
