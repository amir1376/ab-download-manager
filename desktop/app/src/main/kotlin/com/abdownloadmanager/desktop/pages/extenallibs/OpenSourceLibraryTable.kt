package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.desktop.ui.widget.customtable.CellSize
import com.abdownloadmanager.desktop.ui.widget.customtable.SortableCell
import com.abdownloadmanager.desktop.ui.widget.customtable.TableCell
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library

sealed interface LibraryCells : TableCell<Library> {
    data object Name : LibraryCells,
        SortableCell<Library> {
        override fun sortBy(item: Library): Comparable<*> = item.name
        override val name: String = "Name"
        override val size: CellSize = CellSize.Resizeable(100.dp..1000.dp,250.dp)
    }

    data object Author : LibraryCells,
        SortableCell<Library> {
        override fun sortBy(item: Library): Comparable<*> = item.licenses.firstOrNull()?.name.orEmpty()
        override val name: String = "Author"
        override val size: CellSize = CellSize.Resizeable(100.dp..200.dp, 150.dp)
    }
    data object License : LibraryCells,
        SortableCell<Library> {
        override fun sortBy(item: Library): Comparable<*> = item.licenses.firstOrNull()?.name.orEmpty()

        override val name: String = "License"
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
