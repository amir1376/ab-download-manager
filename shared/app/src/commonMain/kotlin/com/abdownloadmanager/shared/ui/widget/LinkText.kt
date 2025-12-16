package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.LocalTextStyle
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.ifThen


@Composable
fun LinkText(
    text: String,
    link: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    showExternalIndicator: Boolean = true,
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
        if (showExternalIndicator) {
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

