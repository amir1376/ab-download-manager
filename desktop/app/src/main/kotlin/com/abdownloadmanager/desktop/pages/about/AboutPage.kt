package com.abdownloadmanager.desktop.pages.about

import androidx.compose.foundation.*
import com.abdownloadmanager.utils.compose.LocalTextStyle
import com.abdownloadmanager.utils.compose.ProvideTextStyle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AboutPage(
    close: () -> Unit,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        RenderAppInfo(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
            onRequestShowTranslators = onRequestShowTranslators,
        )
        Row(Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)) {
            ActionButton(
                myStringResource(Res.string.close),
                onClick = close
            )
        }
    }
}

@Composable
fun RenderAppInfo(
    modifier: Modifier,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Row(
        modifier.fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        ProvideTextStyle(
            TextStyle(fontSize = myTextSizes.base)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        MyIcons.appIcon.rememberPainter(),
                        null,
                        Modifier
                            .size(48.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            AppInfo.displayName,
                            fontSize = myTextSizes.xl,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        WithContentAlpha(0.75f) {
                            Text(
                                myStringResource(
                                    Res.string.version_n,
                                    Res.string.version_n_createArgs(
                                        value = AppInfo.version.toString()
                                    )
                                ), fontSize = myTextSizes.base
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                WithContentAlpha(1f) {
                    Text(myStringResource(Res.string.developed_with_love_for_you))
                    LinkText(myStringResource(Res.string.visit_the_project_website), AppInfo.website)

                    Spacer(Modifier.height(8.dp))

                    Text(myStringResource(Res.string.this_is_a_free_and_open_source_software))
                    LinkText(myStringResource(Res.string.view_the_source_code), AppInfo.sourceCode)
                    Spacer(Modifier.height(8.dp))
                    Text(myStringResource(Res.string.powered_by_open_source_software))
                    Text(
                        myStringResource(Res.string.view_the_open_source_licenses),
                        style = LocalTextStyle.current.merge(LinkStyle),
                        modifier = Modifier.clickable {
                            onRequestShowOpenSourceLibraries()
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(myStringResource(Res.string.localized_by_translators))
                    Text(
                        myStringResource(Res.string.meet_the_translators),
                        style = LocalTextStyle.current.merge(LinkStyle),
                        modifier = Modifier.clickable {
                            onRequestShowTranslators()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LinkText(
    text: String,
    link: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val handler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                handler.openUri(link)
            }
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current
                .merge(LinkStyle).ifThen(isHovered) {
                    copy(
                        textDecoration = TextDecoration.Underline
                    )
                },
            overflow = overflow,
            maxLines = maxLines,
        )
        MyIcon(
            MyIcons.externalLink,
            null,
            Modifier.size(10.dp).alpha(
                if (isHovered) 0.75f
                else 0.5f
            )
        )
    }
}

@Composable
fun MaybeLinkText(
    text: String,
    link: String?,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (link == null) {
        Text(
            modifier = modifier,
            text = text,
            maxLines = maxLines,
            overflow = overflow,
        )
    } else {
        LinkText(
            modifier = modifier,
            text = text,
            link = link,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

private val LinkStyle: TextStyle
    @Composable
    get() = TextStyle(
        color = myColors.info,
    )