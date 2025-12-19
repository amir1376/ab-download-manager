package ir.amirab.util.compose

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

fun Dp.dpToPx(density: Density): Float {
    return with(density) { toPx() }
}

fun Int.pxToDp(density: Density): Dp {
    return with(density) { toDp() }
}
