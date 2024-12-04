package ir.amirab.util.desktop.screen

import androidx.compose.ui.unit.*
//import androidx.compose.ui.window.WindowPlacement
//import androidx.compose.ui.window.WindowPosition
//import androidx.compose.ui.window.WindowState
import ir.amirab.util.desktop.GlobalDensity
import java.awt.GraphicsEnvironment

fun getGlobalScale(): Float {
    val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val defaultScreenDevice = graphicsEnvironment.defaultScreenDevice
    val defaultTransform = defaultScreenDevice.defaultConfiguration.defaultTransform
    return defaultTransform.scaleX.toFloat() // Assuming uniform scaling
}

fun Int.applyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): Int {
    if (userUiScale == null) return this
    return (this * userUiScale / systemUiScale).toInt()
}
fun Float.applyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): Float {
    if (userUiScale == null) return this
    return (this * userUiScale / systemUiScale)
}

fun Int.unApplyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): Int {
    if (userUiScale == null) return this
    return (this * systemUiScale / userUiScale).toInt()
}
fun Float.unApplyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): Float {
    if (userUiScale == null) return this
    return (this * systemUiScale / userUiScale)
}

fun DpSize.applyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): DpSize {
    if (userUiScale == null) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale, systemUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale, systemUiScale).dp
            else it
        },
    )
}

fun DpSize.unApplyUiScale(
    userUiScale: Float?,
    systemUiScale: Float = GlobalDensity.density,
): DpSize {
    if (userUiScale == null) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().unApplyUiScale(userUiScale, systemUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale, systemUiScale).dp
            else it
        },
    )
}

/*
class WindowStateUiScaleAware(
    private val delegate: WindowState,
    private val uiScale: Float?,
) : WindowState {
    override var isMinimized: Boolean
        get() = delegate.isMinimized
        set(value) {
            delegate.isMinimized = value
        }
    override var placement: WindowPlacement
        get() = delegate.placement
        set(value) {
            delegate.placement = value
        }
    override var position: WindowPosition
        get() = delegate.position
        set(value) {
            delegate.position = value
        }
    override var size: DpSize
        get() = run {
            val s = delegate.size
            s.applyUiScale(uiScale)
        }
        set(value) {
            delegate.size = value.unApplyUiScale(uiScale)
        }
}*/
