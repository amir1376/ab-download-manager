package com.abdownloadmanager.desktop.pages.about

import com.abdownloadmanager.utils.compose.LocalTextStyle
import com.abdownloadmanager.utils.compose.ProvideTextStyle
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
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
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.colored.AppIcon
import com.abdownloadmanager.desktop.ui.icons.default.ExternalLink
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AboutPage(
    close: () -> Unit,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        RenderAppInfo(
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
            onRequestShowTranslators = onRequestShowTranslators,
        )
        Spacer(Modifier.weight(1f))
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
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
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
                        imageVector = AbIcons.Colored.AppIcon,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            AppInfo.name,
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
        Icon(
            imageVector = AbIcons.Default.ExternalLink,
            contentDescription = null,
            modifier = Modifier.size(10.dp).alpha(
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