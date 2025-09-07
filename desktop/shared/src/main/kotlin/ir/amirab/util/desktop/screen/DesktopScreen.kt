package ir.amirab.util.desktop.screen

import androidx.compose.ui.unit.*
import com.abdownloadmanager.shared.utils.ui.theme.DEFAULT_UI_SCALE
import java.awt.GraphicsEnvironment

fun getGlobalScale(): Float {
    val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val defaultScreenDevice = graphicsEnvironment.defaultScreenDevice
    val defaultTransform = defaultScreenDevice.defaultConfiguration.defaultTransform
    return defaultTransform.scaleX.toFloat() // Assuming uniform scaling
}

fun Int.applyUiScale(
    userUiScale: Float,
): Int {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this * userUiScale).toInt()
}

fun Float.applyUiScale(
    userUiScale: Float,
): Float {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this * userUiScale)
}

fun Int.unApplyUiScale(
    userUiScale: Float,
): Int {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this / userUiScale).toInt()
}

fun Float.unApplyUiScale(
    userUiScale: Float,
): Float {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this / userUiScale)
}

fun DpSize.applyUiScale(
    userUiScale: Float,
): DpSize {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
    )
}

fun DpSize.unApplyUiScale(
    userUiScale: Float,
): DpSize {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().unApplyUiScale(userUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
    )
}
