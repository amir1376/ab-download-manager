package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.shared.util.ui.ProvideTextStyle
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.table.customtable.Table
import com.abdownloadmanager.shared.ui.widget.table.customtable.TableState
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.table.customtable.styled.MyStyledTableHeader
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import okio.FileSystem
import okio.Path.Companion.toPath

@Composable
internal fun ExternalLibsPage() {
    val libs = rememberLibs()
    OpenSourceLibraries(
        libs = libs,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun OpenSourceLibraries(
    libs: Libs,
    modifier: Modifier,
) {
    var currentDialog by remember {
        mutableStateOf(null as Library?)
    }
    Column(
        modifier
    ) {
        val tableState = remember {
            TableState(
                cells = LibraryCells.all()
            )
        }
        val itemHorizontalPadding = 16.dp
        Table(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(1f),
            list = libs.libraries,
            listState = rememberLazyListState(),
            tableState = tableState,
            wrapHeader = {
                MyStyledTableHeader(
                    itemHorizontalPadding = itemHorizontalPadding,
                    content = it,
                )
            },
            wrapItem = { _, item, rowContent ->
                Box(
                    Modifier
                        .clickable {
                            currentDialog = item
                        }
                        .widthIn(getTableSize().visibleWidth)
                        .padding(vertical = 6.dp, horizontal = itemHorizontalPadding)) {
                    rowContent()
                }
            },
            renderCell = { libraryCell, library ->
                when (libraryCell) {
                    LibraryCells.Name -> {
                        Column {
                            WithContentAlpha(1f) {
                                Row(Modifier) {
                                    Text(
                                        library.name,
                                        fontSize = myTextSizes.base,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    library.artifactVersion?.let { version ->
                                        Text(
                                            text = version,
                                            fontSize = myTextSizes.base,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                            WithContentAlpha(0.75f) {
                                Text(
                                    library.artifactId,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = myTextSizes.sm,
                                )
                            }
                        }
                    }

                    LibraryCells.Author -> {
                        val by = library.by()
                        if (by.isNotEmpty()) {
                            Row {
                                WithContentAlpha(0.7f) {
                                    ProvideTextStyle(
                                        TextStyle(fontSize = myTextSizes.sm)
                                    ) {
                                        for ((name) in by) {
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = name,
                                                fontSize = myTextSizes.base,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LibraryCells.License -> {
                        WithContentAlpha(0.75f) {
                            Text(
                                text = library.licenses.joinToString(", ") { it.name },
                                fontSize = myTextSizes.base,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
        )
    }
    currentDialog.let { library ->
        if (library != null) {
            LibraryDialog(library) {
                currentDialog = null
            }
        }
    }

}

private fun Library.by(): List<Pair<String, String?>> {
    val d = developers.filter {
        it.name != null
    }.map {
        it.name!! to it.organisationUrl
    }.takeIf { it.isNotEmpty() }
    if (d != null) return d
    return organization?.let {
        listOf(it.name to it.url)
    } ?: emptyList()
}

@Composable
private fun rememberLibs(): Libs {
    return remember {
        val jsonContent = FileSystem.RESOURCES.read("aboutlibraries.json".toPath()) {
            readUtf8()
        }
        Libs.Builder().withJson(jsonContent).build()
    }
}
