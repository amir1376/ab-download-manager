package com.abdownloadmanager.shared.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.roundToInt


/**
 * A [PopupPositionProvider] that positions the popup at the given position relative to the anchor.
 *
 * @param positionPx the offset, in pixels, relative to the anchor, to position the popup at.
 * @param offset [DpOffset] to be added to the position of the popup.
 * @param alignment The alignment of the popup relative to desired position.
 * @param windowMargin Defines the area within the window that limits the placement of the popup.
 */
@ExperimentalComposeUiApi
@Composable
fun rememberMyPopupPositionProviderAtPosition(
    positionPx: Offset,
    offset: DpOffset = DpOffset.Zero,
    alignment: Alignment = Alignment.BottomEnd,
    windowMargin: Dp = 4.dp
): PopupPositionProvider = with(LocalDensity.current) {
    val offsetPx = Offset(offset.x.toPx(), offset.y.toPx())
    val windowMarginPx = windowMargin.roundToPx()

    remember(positionPx, offsetPx, alignment, windowMarginPx) {
        PopupPositionProviderAtPosition(
            positionPx = positionPx,
            isRelativeToAnchor = true,
            offsetPx = offsetPx,
            alignment = alignment,
            windowMarginPx = windowMarginPx
        )
    }
}

/**
 * A [PopupPositionProvider] that positions the popup at the given offsets and alignment.
 *
 * @param positionPx The offset of the popup's location, in pixels.
 * @param isRelativeToAnchor Whether [positionPx] is relative to the anchor bounds passed to
 * [calculatePosition]. If `false`, it is relative to the window.
 * @param offsetPx Extra offset to be added to the position of the popup, in pixels.
 * @param alignment The alignment of the popup relative to desired position.
 * @param windowMarginPx Defines the area within the window that limits the placement of the popup,
 * in pixels.
 */
@ExperimentalComposeUiApi
class PopupPositionProviderAtPosition(
    val positionPx: Offset,
    val isRelativeToAnchor: Boolean,
    val offsetPx: Offset,
    val alignment: Alignment = Alignment.BottomEnd,
    val windowMarginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchor = IntRect(
            offset = positionPx.round() +
                    (if (isRelativeToAnchor) anchorBounds.topLeft else IntOffset.Zero),
            size = IntSize.Zero
        )
        val tooltipArea = IntRect(
            IntOffset(
                anchor.left - popupContentSize.width,
                anchor.top - popupContentSize.height,
            ),
            IntSize(
                popupContentSize.width * 2,
                popupContentSize.height * 2
            )
        )
        val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
        var x = tooltipArea.left + position.x + offsetPx.x
        var y = tooltipArea.top + position.y + offsetPx.y
        if (x + popupContentSize.width > windowSize.width - windowMarginPx) {
            x -= popupContentSize.width
        }
        if (y + popupContentSize.height > windowSize.height - windowMarginPx) {
            y -= popupContentSize.height + anchor.height
        }
        x = x.coerceAtLeast(windowMarginPx.toFloat())
        y = y.coerceAtLeast(windowMarginPx.toFloat())

        return IntOffset(x.roundToInt(), y.roundToInt())
    }
}

/**
 * Provides [PopupPositionProvider] relative to the current component bounds.
 *
 * @param anchor The anchor point relative to the current component bounds.
 * @param alignment The alignment of the popup relative to the [anchor] point.
 * @param offset [DpOffset] to be added to the position of the popup.
 */
@Composable
fun rememberMyComponentRectPositionProvider(
    anchor: Alignment = Alignment.BottomCenter,
    alignment: Alignment = Alignment.BottomCenter,
    offset: DpOffset = DpOffset.Zero
): PopupPositionProvider {
    val offsetPx = with(LocalDensity.current) {
        IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
    }
    return remember(anchor, alignment, offsetPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val anchorPoint = anchor.align(IntSize.Zero, anchorBounds.size, layoutDirection)
                val tooltipArea = IntRect(
                    IntOffset(
                        anchorBounds.left + anchorPoint.x - popupContentSize.width,
                        anchorBounds.top + anchorPoint.y - popupContentSize.height,
                    ),
                    IntSize(
                        popupContentSize.width * 2,
                        popupContentSize.height * 2
                    )
                )
                val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
                return tooltipArea.topLeft + position + offsetPx
            }
        }
    }
}

@Composable
fun rememberMyComponentCustomRectPositionProvider(
    providedAnchorBounds: IntRect,
    anchor: Alignment = Alignment.BottomCenter,
    alignment: Alignment = Alignment.BottomCenter,
    offset: DpOffset = DpOffset.Zero
): PopupPositionProvider {
    val offsetPx = with(LocalDensity.current) {
        IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
    }
    return remember(providedAnchorBounds, anchor, alignment, offsetPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val anchorPoint = anchor.align(IntSize.Zero, providedAnchorBounds.size, layoutDirection)
                val tooltipArea = IntRect(
                    IntOffset(
                        providedAnchorBounds.left + anchorPoint.x - popupContentSize.width,
                        providedAnchorBounds.top + anchorPoint.y - popupContentSize.height,
                    ),
                    IntSize(
                        popupContentSize.width * 2,
                        popupContentSize.height * 2
                    )
                )
                val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
                return tooltipArea.topLeft + position + offsetPx
            }
        }
    }
}
