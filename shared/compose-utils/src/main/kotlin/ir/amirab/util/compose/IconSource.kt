package ir.amirab.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

@Immutable
sealed interface IconSource {
    val value: Any
    val requiredTint: Boolean

    @Composable
    fun rememberPainter(): Painter

    @Immutable
    data class StorageIconSource(
        override val value: String,
        override val requiredTint: Boolean,
    ) : IconSource {
        @Composable
        override fun rememberPainter(): Painter = painterResource(value)
    }

    @Immutable
    data class VectorIconSource(
        override val value: ImageVector,
        override val requiredTint: Boolean,
    ) : IconSource {
        @Composable
        override fun rememberPainter(): Painter = rememberVectorPainter(value)
    }
}
