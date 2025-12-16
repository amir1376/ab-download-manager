package com.abdownloadmanager.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ABDMIcons.Alphabet: ImageVector
    get() {
        if (_Alphabet != null) {
            return _Alphabet!!
        }
        _Alphabet = ImageVector.Builder(
            name = "Alphabet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 12f)
                curveTo(9f, 11.735f, 8.895f, 11.481f, 8.707f, 11.293f)
                curveTo(8.519f, 11.105f, 8.265f, 11f, 8f, 11f)
                horizontalLineTo(6f)
                curveTo(5.448f, 11f, 5f, 10.552f, 5f, 10f)
                curveTo(5f, 9.448f, 5.448f, 9f, 6f, 9f)
                horizontalLineTo(8f)
                curveTo(8.796f, 9f, 9.558f, 9.316f, 10.121f, 9.879f)
                curveTo(10.684f, 10.441f, 11f, 11.204f, 11f, 12f)
                verticalLineTo(17f)
                curveTo(11f, 17.552f, 10.552f, 18f, 10f, 18f)
                horizontalLineTo(7f)
                curveTo(6.204f, 18f, 5.442f, 17.684f, 4.879f, 17.121f)
                curveTo(4.316f, 16.559f, 4f, 15.796f, 4f, 15f)
                curveTo(4f, 14.204f, 4.316f, 13.441f, 4.879f, 12.879f)
                curveTo(5.442f, 12.316f, 6.204f, 12f, 7f, 12f)
                horizontalLineTo(9f)
                close()
                moveTo(18f, 12f)
                curveTo(18f, 11.735f, 17.895f, 11.481f, 17.707f, 11.293f)
                curveTo(17.52f, 11.105f, 17.265f, 11f, 17f, 11f)
                horizontalLineTo(16f)
                curveTo(15.735f, 11f, 15.481f, 11.105f, 15.293f, 11.293f)
                curveTo(15.105f, 11.481f, 15f, 11.735f, 15f, 12f)
                verticalLineTo(15f)
                curveTo(15f, 15.265f, 15.105f, 15.519f, 15.293f, 15.707f)
                curveTo(15.481f, 15.895f, 15.735f, 16f, 16f, 16f)
                horizontalLineTo(17f)
                curveTo(17.265f, 16f, 17.52f, 15.895f, 17.707f, 15.707f)
                curveTo(17.895f, 15.519f, 18f, 15.265f, 18f, 15f)
                verticalLineTo(12f)
                close()
                moveTo(6.005f, 15.099f)
                curveTo(6.028f, 15.328f, 6.129f, 15.543f, 6.293f, 15.707f)
                curveTo(6.481f, 15.895f, 6.735f, 16f, 7f, 16f)
                horizontalLineTo(9f)
                verticalLineTo(14f)
                horizontalLineTo(7f)
                curveTo(6.735f, 14f, 6.481f, 14.105f, 6.293f, 14.293f)
                curveTo(6.105f, 14.481f, 6f, 14.735f, 6f, 15f)
                lineTo(6.005f, 15.099f)
                close()
                moveTo(20f, 15f)
                curveTo(20f, 15.796f, 19.684f, 16.559f, 19.121f, 17.121f)
                curveTo(18.559f, 17.684f, 17.796f, 18f, 17f, 18f)
                horizontalLineTo(16f)
                curveTo(15.548f, 18f, 15.109f, 17.895f, 14.709f, 17.704f)
                curveTo(14.528f, 17.886f, 14.277f, 18f, 14f, 18f)
                curveTo(13.448f, 18f, 13f, 17.552f, 13f, 17f)
                verticalLineTo(7f)
                curveTo(13f, 6.448f, 13.448f, 6f, 14f, 6f)
                curveTo(14.552f, 6f, 15f, 6.448f, 15f, 7f)
                verticalLineTo(9.175f)
                curveTo(15.318f, 9.062f, 15.656f, 9f, 16f, 9f)
                horizontalLineTo(17f)
                curveTo(17.796f, 9f, 18.559f, 9.316f, 19.121f, 9.879f)
                curveTo(19.684f, 10.441f, 20f, 11.204f, 20f, 12f)
                verticalLineTo(15f)
                close()
            }
        }.build()

        return _Alphabet!!
    }

@Suppress("ObjectPropertyName")
private var _Alphabet: ImageVector? = null
