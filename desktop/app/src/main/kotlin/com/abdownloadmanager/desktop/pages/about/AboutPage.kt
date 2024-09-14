package com.abdownloadmanager.desktop.pages.about

import com.abdownloadmanager.utils.compose.LocalTextStyle
import com.abdownloadmanager.utils.compose.ProvideTextStyle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
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
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.util.ifThen

@Composable
fun AboutPage(
    close: () -> Unit,
    onRequestShowOpenSourceLibraries: () -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        RenderAppInfo(
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)) {
            ActionButton(
                "Close",
                onClick = close
            )
        }
    }
}

@Composable
fun RenderAppInfo(
    onRequestShowOpenSourceLibraries: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp)
        ,
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
                            AppInfo.name,
                            fontSize = myTextSizes.xl,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        WithContentAlpha(0.75f) {
                            Text("version ${AppInfo.version}", fontSize = myTextSizes.base)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                WithContentAlpha(1f) {
                    Text("Developed with ❤️ for you")
                    LinkText("Visit the project website", AppInfo.website)

                    Spacer(Modifier.height(8.dp))

                    Text("This is a free & Open Source software")
                    LinkText("See the Source Code", AppInfo.sourceCode)
                    Spacer(Modifier.height(8.dp))
                    Text("Powered by Open Source Libraries")
                    Text(
                        "See the Open Sources libraries",
                        style = LocalTextStyle.current.merge(LinkStyle),
                        modifier = Modifier.clickable {
                            onRequestShowOpenSourceLibraries()
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
                .merge(LinkStyle).ifThen(isHovered){
                    copy(
                        textDecoration = TextDecoration.Underline
                    )
                }
            ,
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