package com.abdownloadmanager.desktop.pages.about

import androidx.compose.foundation.*
import com.abdownloadmanager.shared.utils.ui.LocalTextStyle
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Tooltip
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import ir.amirab.util.URLOpener
import ir.amirab.util.UrlUtils
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AboutPage(
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Box {
        BackgroundEffects()
        RenderAppInfo(
            modifier = Modifier,
            onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
            onRequestShowTranslators = onRequestShowTranslators,
        )
    }
}

@Composable
private fun AppIconAndVersion(
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(
            horizontal = 24.dp,
            vertical = 8.dp,
        )
    ) {
        val shape = RoundedCornerShape(16.dp)
        Image(
            MyIcons.appIcon.rememberPainter(),
            null,
            Modifier
                .shadow(12.dp, shape, spotColor = myColors.primary)
                .clip(shape)
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(myColors.primary, myColors.secondary)
                    ),
                    shape
                )
                .background(myColors.surface)
                .padding(16.dp)
                .size(52.dp)
        )
        Spacer(Modifier.size(16.dp))
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                AppInfo.displayName,
                fontSize = myTextSizes.lg,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            WithContentAlpha(0.75f) {
                Text(
                    myStringResource(
                        Res.string.version_n,
                        Res.string.version_n_createArgs(
                            value = AppInfo.version.toString(),
                        )
                    ),
                    fontSize = myTextSizes.base,
                )
            }
        }
    }
}

@Composable
private fun RenderAppInfo(
    modifier: Modifier,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Row(
        modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.width(250.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            AppIconAndVersion(Modifier.fillMaxWidth())
            Spacer(Modifier.weight(1f))
            Column(
                Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    myStringResource(Res.string.developed_with_love_for_you),
                )
                Spacer(Modifier.height(8.dp))
                DonateButton()
                Spacer(Modifier.height(8.dp))
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .background(myColors.onBackground / 0.05f)
                        .height(1.dp)
                )
                Spacer(Modifier.height(8.dp))
                val websiteUrl = SharedConstants.projectWebsite
                val websiteDisplayName = remember(websiteUrl) {
                    UrlUtils.getHost(websiteUrl) ?: websiteUrl
                }
                LinkText(
                    text = websiteDisplayName,
                    link = websiteUrl,
                    showExternalIndicator = false,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        Spacer(
            Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(myColors.onBackground.copy(0.15f))
        )
        Column(
            Modifier.weight(1f)
        ) {
            CreditsSection(
                modifier = Modifier.fillMaxWidth().weight(1f),
                onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
                onRequestShowTranslators = onRequestShowTranslators,
            )
            Spacer(Modifier.height(1.dp).fillMaxWidth().background(myColors.onBackground / 0.15f))
            SocialAndLinks(
                Modifier
                    .fillMaxWidth()
                    .background(myColors.surface / 0.5f)
                    .padding(top = 12.dp)
                    .padding(bottom = 16.dp)
                    .wrapContentWidth(),
                horizontalPadding = 8.dp,
            )
        }
    }
}

@Composable
private fun SocialAndLinks(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(
                horizontal = horizontalPadding,
            )
    ) {
        SocialSmallButton(
            MyIcons.earth,
            Res.string.visit_the_project_website.asStringSource(),
            onClick = {
                URLOpener.openUrl(SharedConstants.projectWebsite)
            }
        )
        SocialSmallButton(
            MyIcons.openSource,
            Res.string.view_the_source_code.asStringSource(),
            onClick = {
                URLOpener.openUrl(SharedConstants.projectSourceCode)
            }
        )
        SocialSmallButton(
            MyIcons.speaker,
            Res.string.channel.asStringSource(),
            onClick = {
                URLOpener.openUrl(SharedConstants.telegramChannelUrl)
            }
        )
        SocialSmallButton(
            MyIcons.group,
            Res.string.group.asStringSource(),
            onClick = {
                URLOpener.openUrl(SharedConstants.telegramGroupUrl)
            }
        )
        SocialSmallButton(
            MyIcons.language,
            Res.string.translators_contribute_title.asStringSource(),
            onClick = {
                URLOpener.openUrl(SharedConstants.projectTranslations)
            }
        )
    }
}

@Composable
private fun CreditsSection(
    modifier: Modifier = Modifier,
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val itemModifier = Modifier.fillMaxWidth()
        AboutPageListItemButton(
            itemModifier,
            icon = MyIcons.hearth,
            title = Res.string.this_is_a_free_and_open_source_software.asStringSource(),
            description = Res.string.view_the_source_code.asStringSource(),
            onClick = {
                URLOpener.openUrl(AppInfo.sourceCode)
            }
        )
        AboutPageListItemButton(
            itemModifier,
            icon = MyIcons.openSource,
            title = Res.string.powered_by_open_source_software.asStringSource(),
            description = Res.string.view_the_open_source_licenses.asStringSource(),
            onClick = {
                onRequestShowOpenSourceLibraries()
            }
        )
        AboutPageListItemButton(
            itemModifier,
            icon = MyIcons.language,
            title = Res.string.localized_by_translators.asStringSource(),
            description = Res.string.meet_the_translators.asStringSource(),
            onClick = {
                onRequestShowTranslators()
            }
        )
    }
}

@Composable
private fun SocialSmallButton(
    icon: IconSource,
    title: StringSource,
    onClick: () -> Unit,
) {
    Tooltip(title) {
        IconActionButton(
            icon,
            contentDescription = title.rememberString(),
            onClick = onClick,
        )
    }
}

@Composable
private fun AboutPageListItemButton(
    modifier: Modifier,
    icon: IconSource,
    title: StringSource,
    description: StringSource,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier
            .border(1.dp, myColors.onBackground / 0.15f, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(myColors.surface / 0.5f)
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                title.rememberString(),
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(description.rememberString())
        }
    }
}


@Composable
private fun BoxScope.BackgroundEffects() {
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .offset(x = (-50).dp, y = (-148).dp)
            .fillMaxWidth(0.5f)
            .height(250.dp)
            .blur(
                56.dp,
                edgeTreatment = BlurredEdgeTreatment.Unbounded
            )
            .clip(CircleShape)
            .background(
                myColors.primary / 0.15f
            )
    )
    Box(
        Modifier
            .align(Alignment.BottomStart)
            .size(220.dp)
            .offset(x = (-64).dp, y = (+128).dp)
            .blur(
                56.dp,
                edgeTreatment = BlurredEdgeTreatment.Unbounded
            )
            .clip(CircleShape)
            .background(
                myColors.secondaryVariant / 0.15f
            )
    )
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .size(220.dp)
            .offset(x = 32.dp, y = (-32).dp)
            .blur(
                56.dp,
                edgeTreatment = BlurredEdgeTreatment.Unbounded
            )
            .clip(CircleShape)
            .background(
                myColors.secondary / 0.15f
            )
    )
}


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

@Composable
private fun DonateButton() {
    ActionButton(
        backgroundColor = SolidColor(LocalContentColor.current / 0.05f),
        start = {
            MyIcon(
                MyIcons.hearth,
                null,
                modifier = Modifier.size(16.dp),
                tint = myColors.error,
            )
            Spacer(Modifier.width(8.dp))
        },
        text = myStringResource(Res.string.donate),
        onClick = {
            URLOpener.openUrl(SharedConstants.donateLink)
        }
    )
}

private val LinkStyle: TextStyle
    @Composable
    get() = TextStyle(
        color = myColors.info,
    )
