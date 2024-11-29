package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.desktop.ui.widget.customtable.CellSize
import com.abdownloadmanager.desktop.ui.widget.customtable.SortableCell
import com.abdownloadmanager.desktop.ui.widget.customtable.TableCell
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.mikepenz.aboutlibraries.entity.Library
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

sealed interface LibraryCells : TableCell<Library> {
    data object Name : LibraryCells,
        SortableCell<Library> {
        override fun comparator(): Comparator<Library> = compareBy { it.name }
        override val id: String = "Name"
        override val name: StringSource = Res.string.name.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..1000.dp, 250.dp)
    }

    data object Author : LibraryCells,
        SortableCell<Library> {
        override fun comparator(): Comparator<Library> = compareBy { item ->
            item.licenses.firstOrNull()?.name.orEmpty()
        }

        override val id: String = "Author"
        override val name: StringSource = Res.string.author.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..200.dp, 150.dp)
    }

    data object License : LibraryCells,
        SortableCell<Library> {
        override fun comparator(): Comparator<Library> = compareBy { it.licenses.firstOrNull()?.name.orEmpty() }

        override val id: String = "License"
        override val name: StringSource = Res.string.license.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..200.dp, 150.dp)
    }

    companion object {
        fun all() = listOf(
            Name,
            Author,
            License,
        )
    }
}
