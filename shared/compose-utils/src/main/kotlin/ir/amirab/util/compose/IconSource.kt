package ir.amirab.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import ir.amirab.util.compose.contants.RESOURCE_PROTOCOL
import okio.FileSystem
import okio.Path.Companion.toPath
import java.net.URI

@Immutable
sealed interface IconSource {
    val value: Any
    val requiredTint: Boolean

    @Composable
    fun rememberPainter(): Painter

    @Immutable
    data class ResourceIconSource(
        override val value: String,
        override val requiredTint: Boolean,
    ) : IconSourceWithURI {
        @Composable
        override fun rememberPainter(): Painter = painterResource(value)
        override fun toUri() = "$RESOURCE_PROTOCOL:$value?tint=${requiredTint}"
        override fun exists(): Boolean {
            return FileSystem.RESOURCES.exists(value.toPath())
        }
    }

    @Immutable
    data class VectorIconSource(
        override val value: ImageVector,
        override val requiredTint: Boolean,
    ) : IconSource {
        @Composable
        override fun rememberPainter(): Painter = rememberVectorPainter(value)
    }

    companion object
}

interface IconSourceWithURI : IconSource {
    fun toUri(): String
    fun exists(): Boolean
}

fun IconSource.uriOrNull() = (this as? IconSourceWithURI)?.toUri()

@Suppress("NAME_SHADOWING")
fun IconSource.Companion.fromUri(uri: String): IconSourceWithURI? {
    val uri = URI(uri)
    return when (uri.scheme) {
        RESOURCE_PROTOCOL -> IconSource.ResourceIconSource(
            value = uri.path,
//            requiredTint = uri.query["tint"]?.toBooleanOrNull()?:true,
            requiredTint = true,
        )

        else -> null
//        else -> kotlin.runCatching { uri.toURL() }
//            .getOrNull()
//            ?.openStream()
    }?.takeIf { it.exists() }
}