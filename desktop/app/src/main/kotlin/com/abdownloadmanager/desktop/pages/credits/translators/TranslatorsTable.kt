package com.abdownloadmanager.desktop.pages.credits.translators

import com.abdownloadmanager.shared.ui.widget.customtable.CellSize
import com.abdownloadmanager.shared.ui.widget.customtable.SortableCell
import com.abdownloadmanager.shared.ui.widget.customtable.TableCell
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

sealed interface TranslatorsCells : TableCell<LanguageTranslationInfo> {
    data object LanguageName : TranslatorsCells,
        SortableCell<LanguageTranslationInfo> {
        override fun comparator(): Comparator<LanguageTranslationInfo> = compareBy { it.locale }
        override val id: String = "language"
        override val name: StringSource = Res.string.language.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..1000.dp, 200.dp)
    }

    data object Translators : TranslatorsCells {
        override val id: String = "translators"
        override val name: StringSource = Res.string.translators.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..1000.dp, 350.dp)
    }

    companion object {
        fun all() = listOf(
            LanguageName,
            Translators,
        )
    }
}
