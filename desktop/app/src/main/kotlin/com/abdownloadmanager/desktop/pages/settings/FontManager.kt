package com.abdownloadmanager.desktop.pages.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FileFont
import androidx.compose.ui.text.platform.ResourceFont
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.contants.FILE_PROTOCOL
import ir.amirab.util.compose.contants.RESOURCE_PROTOCOL
import ir.amirab.util.compose.contants.SYSTEM_PROTOCOL
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.awt.GraphicsEnvironment
import java.net.URI
import kotlin.io.path.toPath


class FontManager(
    private val appSettings: AppSettingsStorage,
) {
    companion object {
        private const val DEFAULT_FONT_ID = "default"
        val defaultFontInfo = FontInfo(
            id = DEFAULT_FONT_ID,
            uri = "",
            name = Res.string.default.asStringSource(),
            fontFamily = FontFamily.Default,
        )

        fun getUsableFontFamilyNamesOfSystem(): List<String> {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
                .toList()
        }
    }

    private val _availableFonts = MutableStateFlow(emptyList<FontInfo>())
    val availableFonts = _availableFonts.asStateFlow()

    private fun getFontByUri(uri: String): FontFamily? {
        return runCatching {
            FontFamilyUtil.fromUri(URI.create(uri))
        }
            .onFailure {
                it.printStackTrace()
            }
            .getOrNull()
    }

    val selectableFonts = availableFonts.mapStateFlow {
        buildList {
            add(defaultFontInfo)
            addAll(it)
        }
    }

    val currentFontInfo = combineStateFlows(
        appSettings.font,
        selectableFonts,
    ) { fontId, possibleFonts ->
        val fontId = fontId ?: DEFAULT_FONT_ID
        possibleFonts.find {
            it.id == fontId
        } ?: possibleFonts.find {
            it.id == DEFAULT_FONT_ID
        }!!
    }

    val currentFontFamily = currentFontInfo.mapStateFlow {
        it.fontFamily
    }

    fun setFont(fontId: String?) {
        synchronized(this) {
            val fontId = fontId ?: DEFAULT_FONT_ID
            val font = availableFonts.value.find { it.id == fontId }
                ?: defaultFontInfo

            appSettings.font.value = font.takeIf {
                it != defaultFontInfo
            }?.uri
        }
    }

    @Volatile
    private var booted = false

    fun boot() {
        if (booted) return

        val systemFontFamilies = getUsableFontFamilyNamesOfSystem()
            .mapNotNull { fontFamilyName ->
                val uri = runCatching {
                    URI(SYSTEM_PROTOCOL, fontFamilyName, null).toString()
                }.onFailure { throwable ->
                    // it seems that some fonts has empty name, which causes URI creation to fail
                    // in order to not break the app, we will just ignore those fonts
                    println("system font family with name:\"$fontFamilyName\" can't be used: $throwable")
                }.getOrNull()
                if (uri == null) {
                    return@mapNotNull null
                }
                val fontFamily = getFontByUri(uri)
                if (fontFamily == null) {
                    return@mapNotNull null
                }
                FontInfo(
                    id = uri,
                    uri = uri,
                    name = fontFamilyName.asStringSource(),
                    fontFamily = fontFamily,
                )
            }

        _availableFonts.update {
            it.plus(systemFontFamilies)
        }
        setFont(appSettings.font.value)
        booted = true
    }

}

/**
 * This is for demonstration purposes of a font
 */
@Immutable
data class FontInfo(
    val id: String,
    val uri: String,
    val name: StringSource,
    val fontFamily: FontFamily,
)

private object FontFamilyUtil {
    @OptIn(ExperimentalTextApi::class)
    fun fromUri(uri: URI): FontFamily {
        return when (uri.scheme) {
            FILE_PROTOCOL -> {
                return FontFamily(
                    FileFont(uri.toPath().toFile())
                )
            }

            RESOURCE_PROTOCOL -> {
                val path = uri.schemeSpecificPart
                require(path.isNotEmpty())
                FontFamily(
                    ResourceFont(uri.schemeSpecificPart)
                )
            }

            SYSTEM_PROTOCOL -> {
                // This is a system font, we can use it directly
                val name = uri.schemeSpecificPart
                require(name.isNotEmpty())
                FontFamily(name)
            }

            else -> throw IllegalArgumentException("Unsupported font URI scheme: ${uri.scheme}")
        }
    }
}
