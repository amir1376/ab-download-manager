package ir.amirab.util.compose.layout

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

class RelativeAlignment(
    val mainAlignment: Alignment,
    val relative: IntOffset,
) : Alignment {
    override fun align(
        size: IntSize,
        space: IntSize,
        layoutDirection: LayoutDirection
    ): IntOffset {
        val result = mainAlignment.align(size, space, layoutDirection)
        val resultWithOffset = result + relative
        return IntOffset(
            resultWithOffset.x.coerceIn(0, space.width),
            resultWithOffset.y.coerceIn(0, space.height),
        )
    }


    class Horizontal(
        val mainAlignment: Alignment.Horizontal,
        val relative: Int,
    ) : Alignment.Horizontal {
        override fun align(
            size: Int,
            space: Int,
            layoutDirection: LayoutDirection
        ): Int {
            val result = mainAlignment.align(size, space, layoutDirection)
            val resultWithOffset = result + relative
            return resultWithOffset.coerceIn(0..space)
        }
    }

    class Vertical(
        val mainAlignment: Alignment.Vertical,
        val relative: Int,
    ) : Alignment.Vertical {
        override fun align(
            size: Int,
            space: Int,
        ): Int {
            val result = mainAlignment.align(size, space)
            val resultWithOffset = result + relative
            return resultWithOffset.coerceIn(0..space)
        }
    }
}
