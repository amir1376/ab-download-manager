package com.abdownloadmanager.android.pages.credits.thirdpartylibraries

import androidx.compose.foundation.background
import com.abdownloadmanager.shared.util.ui.ProvideTextStyle
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen


@Composable
fun ThirdPartyLibrariesPage() {
    val pageTitle = myStringResource(Res.string.open_source_software_used_in_this_app)
    Column(
        Modifier
            .fillMaxSize()
            .background(myColors.background)
            .systemBarsPadding()
            .navigationBarsPadding(),
    ) {
        PageTitle(pageTitle)
        OpenSourceLibraries(
            libs = rememberLibs(),
            modifier = Modifier
        )
    }
}

@Composable
private fun OpenSourceLibraries(
    libs: Libs,
    modifier: Modifier,
) {
    val dividerColor = myColors.onBackground / 0.5f
    var currentDialog by remember {
        mutableStateOf(null as Library?)
    }
    Column(modifier) {
        LazyColumn {
            itemsIndexed(libs.libraries) { index, item ->
                val isFirstItem = index == 0
                RenderLibraryItemInList(
                    item,
                    Modifier
                        .ifThen(!isFirstItem) {
                            drawBehind {
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            dividerColor,
                                            Color.Transparent,
                                        )
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, 0f)
                                )
                            }
                        }
                        .fillMaxWidth()
                        .clickable {
                            currentDialog = item
                        }
                        .padding(mySpacings.largeSpace)
                )
            }
        }
    }
    LibraryDialog(
        library = currentDialog,
        onCloseRequest = {
            currentDialog = null
        }
    )

}

@Composable
private fun RenderLibraryItemInList(
    library: Library,
    modifier: Modifier,
) {
    Column(modifier) {
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
            Spacer(Modifier.height(mySpacings.mediumSpace))
            WithContentAlpha(0.75f) {
                Text(
                    library.artifactId,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = myTextSizes.sm,
                )
            }
        }
        val by = library.by()
        if (by.isNotEmpty()) {
            Spacer(Modifier.height(mySpacings.mediumSpace))
            Row {
                WithContentAlpha(0.7f) {
                    ProvideTextStyle(
                        TextStyle(fontSize = myTextSizes.sm)
                    ) {
                        for ((index, item) in by.withIndex()) {
                            val (name, _) = item
                            if (index != 0) {
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                text = name,
                                fontSize = myTextSizes.sm,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(mySpacings.mediumSpace))
        WithContentAlpha(0.75f) {
            Text(
                text = library.licenses.joinToString(", ") { it.name },
                fontSize = myTextSizes.sm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    val resources = LocalResources.current
    return remember {
        val jsonContent = resources
            .openRawResource(com.abdownloadmanager.android.R.raw.aboutlibraries)
            .bufferedReader()
            .use { it.readText() }
        Libs.Builder().withJson(jsonContent).build()
    }
}
