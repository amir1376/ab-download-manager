package com.abdownloadmanager.desktop.pages.extenallibs

import com.abdownloadmanager.desktop.pages.about.MaybeLinkText
import com.abdownloadmanager.shared.utils.ui.ProvideTextStyle
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abdownloadmanager.resources.Res
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Organization
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun LibraryDialog(
    library: Library, onCloseRequest: () -> Unit,
) {
    Dialog(
        onCloseRequest, properties = DialogProperties()
    ) {
        ProvideTextStyle(
            TextStyle(fontSize = myTextSizes.base)
        ) {
            val shape = RoundedCornerShape(6.dp)
            Column(
                Modifier
                    .clip(shape)
                    .border(2.dp, myColors.onBackground / 10, shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                myColors.surface,
                                myColors.background,
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    LibraryNameAndVersion(library.name, library.artifactVersion, library.artifactId)
                    Spacer(Modifier.height(16.dp))
                    library.description?.let {
                        LibraryDescription(it)
                    }
                    Spacer(Modifier.height(16.dp))
                    library.developers.takeIf { it.isNotEmpty() }?.let {
                        LibraryDevelopers(it)
                    }
                    library.organization?.let {
                        LibraryOrganization(it)
                    }
                    val links = buildList {
                        library.scm?.url?.let {
                            add(Res.string.source_code.asStringSource() to it)
                        }
                        library.website?.let {
                            add(Res.string.website.asStringSource() to it)
                        }
                    }
                    links.takeIf { it.isNotEmpty() }?.let {
                        LibraryLinks(links)
                    }
                    LibraryLicenseInfo(library.licenses)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ActionButton(myStringResource(Res.string.close), onClick = {
                        onCloseRequest()
                    })
                }
            }
        }
    }
}

@Composable
private fun LibraryLinks(links: List<Pair<StringSource, String>>) {
    KeyValue(myStringResource(Res.string.links)) {
        ListOfNamesWithLinks(links)
    }
}

@Composable
private fun LibraryDescription(description: String) {
    Text(
        description,
        modifier = Modifier.background(myColors.surface).padding(8.dp),
        color = myColors.onSurface,
    )
}

@Composable
private fun LibraryLicenseInfo(licenses: ImmutableSet<License>) {
    KeyValue(myStringResource(Res.string.license)) {
        val l = licenses.map {
            it.name.asStringSource() to it.url
        }
        if (l.isEmpty()) {
            Text(myStringResource(Res.string.no_license_found))
        } else {
            ListOfNamesWithLinks(l)
        }
    }
}

@Composable
private fun LibraryDevelopers(devs: List<Developer>) {
    KeyValue(myStringResource(Res.string.developers)) {
        ListOfNamesWithLinks(
            devs
                .filter { it.name != null }
                .map {
                    it.name!!.asStringSource() to it.organisationUrl
                }
        )
    }
}

@Composable
private fun ListOfNamesWithLinks(map: List<Pair<StringSource, String?>>) {
    Row {
        for ((i, v) in map.withIndex()) {
            val (name, link) = v
            MaybeLinkText(name.rememberString(), link)
            if (i < map.lastIndex) {
                Text(", ")
            }
        }
    }
}

@Composable
fun LibraryOrganization(organization: Organization) {
    KeyValue(myStringResource(Res.string.organization)) {
        MaybeLinkText(organization.name, organization.url)
    }
}

@Composable
private fun LibraryNameAndVersion(
    name: String, version: String?,
    artifactId: String,
) {
    val nameWithVersion = name + (version?.let { " $it" }.orEmpty())
    Column {
        Row {
            Text(
                "$nameWithVersion",
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.base,
            )
        }
        Spacer(Modifier.height(4.dp))
        WithContentAlpha(0.75f) {
            Row {
                Text(
                    "($artifactId)",
                    fontSize = myTextSizes.sm,
                )
            }
        }
    }
}

@Composable
private fun KeyValue(
    key: String,
    value: @Composable () -> Unit,
) {
    Row {
        WithContentAlpha(0.75f) {
            Text(
                "$key:",
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        value()
    }
}