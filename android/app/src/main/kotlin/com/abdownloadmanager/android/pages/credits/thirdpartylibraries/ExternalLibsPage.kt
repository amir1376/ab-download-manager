package com.abdownloadmanager.android.pages.credits.thirdpartylibraries

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import com.abdownloadmanager.shared.util.ui.ProvideTextStyle
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.page.FooterFade
import com.abdownloadmanager.android.ui.page.HeaderFade
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.resources.Res
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.android.ui.page.PageTitleWithDescription
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.android.ui.page.rememberHeaderAlpha
import com.abdownloadmanager.android.util.compose.useBack
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.dpToPx
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen


@Composable
fun ThirdPartyLibrariesPage() {
    val pageTitle = myStringResource(Res.string.third_party_libraries)
    val pageDescription = myStringResource(Res.string.powered_by_open_source_software)
    val onBack = useBack()
    var contentPadding by remember { mutableStateOf(PaddingValues.Zero) }
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val headerAlpha by rememberHeaderAlpha(listState, topPadding.dpToPx(density))
    PageUi(
        header = {
            PageHeader(
                leadingIcon = {
                    TransparentIconActionButton(
                        MyIcons.back,
                        myStringResource(Res.string.back)
                    ) {
                        onBack?.onBackPressed()
                    }
                },
                headerTitle = {
                    PageTitleWithDescription(pageTitle, pageDescription)
                },
                modifier = Modifier
                    .background(
                        myColors.background.copy(
                            alpha = headerAlpha * 0.75f,
                        )
                    )
                    .statusBarsPadding()
            )
        },
        footer = {
            Spacer(Modifier.navigationBarsPadding())
        }
    ) {
        contentPadding = it.paddingValues
        Box(
            Modifier
                .fillMaxSize()
        ) {
            OpenSourceLibraries(
                libs = rememberLibs(),
                modifier = Modifier,
                state = listState,
                contentPadding = it.paddingValues,
            )
            FooterFade(bottomPadding)
        }
    }
}

@Composable
private fun OpenSourceLibraries(
    libs: Libs,
    modifier: Modifier,
    state: LazyListState,
    contentPadding: PaddingValues,
) {
    val dividerColor = myColors.onBackground / 0.5f
    var currentDialog by remember {
        mutableStateOf(null as Library?)
    }
    Column(modifier) {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
        ) {
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
