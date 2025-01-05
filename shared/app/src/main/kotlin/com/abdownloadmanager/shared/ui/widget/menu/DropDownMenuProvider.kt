import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider

@Immutable
internal class DropdownMenuPositionProvider() : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return anchorBounds.bottomLeft
    }
}