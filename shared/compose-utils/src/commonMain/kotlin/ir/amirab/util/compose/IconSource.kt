package ir.amirab.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Immutable
sealed interface IconSource {
    val value: Any
    val requiredTint: Boolean
    val uri: String?

    @Composable
    fun rememberPainter(): Painter

    @Immutable
    data class VectorIconSource(
        override val value: ImageVector,
        override val requiredTint: Boolean,
        override val uri: String? = null,
    ) : IconSource {
        @Composable
        override fun rememberPainter(): Painter = rememberVectorPainter(value)
    }

    @Immutable
    data class PainterIconSource(
        override val value: Painter,
        override val requiredTint: Boolean,
        override val uri: String? = null,
    ) : IconSource {
        @Composable
        override fun rememberPainter(): Painter = value
    }

    companion object
}
