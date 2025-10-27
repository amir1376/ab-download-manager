package ir.amirab.util.compose

import androidx.compose.runtime.staticCompositionLocalOf

interface IIconResolver {
    fun resolve(uri: String): IconSource?
}

val LocalIconFromUriResolver = staticCompositionLocalOf<IIconResolver> {
    error("LocalIconFromUriResolver not provided")
}
