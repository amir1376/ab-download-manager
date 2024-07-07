package com.abdownloadmanager.desktop.utils

import androidx.compose.ui.graphics.Color

operator fun Color.div(percent: Int): Color = run {
    require(percent in 0..100)
    div(percent.toFloat() / 100)
}

operator fun Color.div(percent: Float): Color = run {
    require(percent in 0f..1f)
    copy(alpha = percent)
}

fun Color.lighter(amount: Float = 0.1f): Color {
    return toHsl().apply { laminate += amount }.toColor()
}

fun Color.darker(amount: Float = 0.1f): Color {
    return toHsl().apply { laminate -= amount }.toColor()
}


@JvmInline
value class HSLColor(val hsl: FloatArray) {
    constructor(hue: Float, saturation: Float, laminate: Float) : this(
        floatArrayOf(hue, saturation, laminate)
    )
    constructor(hue: Int, saturation: Float, laminate: Float) : this(
        floatArrayOf(hue/360f, saturation, laminate)
    )

    fun copy(): HSLColor {
        return HSLColor(hsl.copyOf())
    }

    override fun toString(): String {
        return """HSLColor( hue=$hue , saturation=$saturation , laminate=$laminate )"""
    }
    fun getHueInt():Int{
        return (hue*360).toInt()
    }
    fun setHue(value: Int) {
        val h = value.coerceIn(0..360)
        hue = h / 360f
    }

    var hue
        get() = hsl[0]
        set(value) {
            hsl[0] = value.coerceIn(0f, 1f)
        }
    var saturation
        get() = hsl[1]
        set(value) {
            hsl[1] = value.coerceIn(0f, 1f)
        }
    var laminate
        get() = hsl[2]
        set(value) {
            hsl[2] = value.coerceIn(0f, 1f)
        }
}


fun Color.toHsl(): HSLColor {
    val hsl = FloatArray(3)
    val color = this

    val r = color.red
    val g = color.green
    val b = color.blue

    val max = Math.max(r, Math.max(g, b))
    val min = Math.min(r, Math.min(g, b))
    hsl[2] = (max + min) / 2

    if (max == min) {
        hsl[1] = 0f
        hsl[0] = hsl[1]

    } else {
        val d = max - min

        hsl[1] = if (hsl[2] > 0.5f) d / (2f - max - min) else d / (max + min)
        when (max) {
            r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
            g -> hsl[0] = (b - r) / d + 2
            b -> hsl[0] = (r - g) / d + 4
        }
        hsl[0] /= 6f
    }
    return HSLColor(hsl)
}

fun HSLColor.toColor(): Color {
    val hsl: FloatArray = this.hsl
    val r: Float
    val g: Float
    val b: Float

    val h = hsl[0]
    val s = hsl[1]
    val l = hsl[2]

    if (s == 0f) {
        b = l
        g = b
        r = g
    } else {
        val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        r = hue2rgb(p, q, h + 1f / 3)
        g = hue2rgb(p, q, h)
        b = hue2rgb(p, q, h - 1f / 3)
    }

    return Color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
}

private fun hue2rgb(p: Float, q: Float, t: Float): Float {
    var valueT = t
    if (valueT < 0) valueT += 1f
    if (valueT > 1) valueT -= 1f
    if (valueT < 1f / 6) return p + (q - p) * 6f * valueT
    if (valueT < 1f / 2) return q
    return if (valueT < 2f / 3) p + (q - p) * (2f / 3 - valueT) * 6f else p
}
