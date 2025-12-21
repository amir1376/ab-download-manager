package com.abdownloadmanager.android.pages.about

import androidx.compose.runtime.Composable


import androidx.compose.foundation.*
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.page.PageFooter
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.android.ui.page.createAlphaForHeader
import com.abdownloadmanager.android.ui.page.rememberHeaderAlpha
import com.abdownloadmanager.android.util.compose.useBack
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Tooltip
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.URLOpener
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.dpToPx
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AboutPage(
    onRequestShowOpenSourceLibraries: () -> Unit,
    onRequestShowTranslators: () -> Unit,
) {
    val state = rememberScrollState()
    var paddings by remember { mutableStateOf(PaddingValues.Zero) }
    val headerAlpha =
        createAlphaForHeader(state.value.toFloat(), paddings.calculateTopPadding().dpToPx(LocalDensity.current))
    val shape = myShapes.defaultRounded
    PageUi(
        header = {
            val onBack = useBack()
            PageHeader(
                leadingIcon = {
                    TransparentIconActionButton(
                        icon = MyIcons.back,
                        contentDescription = myStringResource(Res.string.back)
                    ) {
                        onBack?.onBackPressed()
                    }
                },
                headerTitle = {
                    PageTitle(myStringResource(Res.string.about))
                },
                modifier = Modifier
                    .background(
                        myColors.background.copy(
                            alpha = headerAlpha * 0.75f
                        )
                    )
                    .statusBarsPadding(),

            )
        },
        footer = {
            PageFooter {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = mySpacings.largeSpace)
                        .padding(bottom = mySpacings.largeSpace)
                        .border(1.dp, myColors.onBackground / 0.15f, shape)
                        .clip(shape)
                        .background(myColors.surface)
                ) {
                    Spacer(Modifier.height(mySpacings.largeSpace))
                    DevelopedWithLove(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                    )
                    Spacer(Modifier.height(mySpacings.mediumSpace))
                    SocialAndLinks(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(),
                        horizontalPadding = 8.dp,
                    )
                    Spacer(Modifier.height(mySpacings.mediumSpace))
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .background(myColors.onBackground / 0.05f)
                            .height(1.dp)
                    )
                    MainWebsite(Modifier)
                }
            }
        }
    ) {
        paddings = it.paddingValues
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(state)
                .padding(it.paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = mySpacings.largeSpace),
            ) {
                AppIconAndVersion(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
            CreditsSection(
                modifier = Modifier
                    .fillMaxWidth(),
                onRequestShowOpenSourceLibraries = onRequestShowOpenSourceLibraries,
                onRequestShowTranslators = onRequestShowTranslators,
            )
        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                SharedConstants.appDisplayName,
                fontSize = myTextSizes.lg,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            WithContentAlpha(0.75f) {
                Text(
                    myStringResource(
                        Res.string.version_n,
                        Res.string.version_n_createArgs(
                            value = AppVersion.get().toString(),
                        )
                    ),
                    fontSize = myTextSizes.base,
                )
            }
        }
    }
}


@Composable
fun MainWebsite(
    modifier: Modifier
) {
    val uriHandler = LocalUriHandler.current
    val websiteUrl = SharedConstants.projectWebsite
    val websiteDisplayName = remember(websiteUrl) {
        HttpUrlUtils.getHost(websiteUrl) ?: websiteUrl
    }
    Column(
        modifier
            .fillMaxWidth()
            .clickable {
                uriHandler.openUri(websiteUrl)
            }
            .padding(
                mySpacings.largeSpace
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = websiteDisplayName,
            color = myColors.info,
        )
    }
}

@Composable
fun DevelopedWithLove(modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            myStringResource(Res.string.developed_with_love_for_you),
            Modifier
                .fillMaxWidth()
                .wrapContentWidth()
        )
        Spacer(Modifier.height(mySpacings.largeSpace))
        DonateButton(Modifier)
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
                URLOpener.openUrl(SharedConstants.projectSourceCode)
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
    val shape = myShapes.defaultRounded
    Row(
        modifier
            .border(1.dp, myColors.onBackground / 0.15f, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(myColors.surface)
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
            Spacer(Modifier.height(4.dp))
            WithContentAlpha(0.75f) {
                Text(description.rememberString())
            }
        }
    }
}


@Composable
private fun DonateButton(
    modifier: Modifier,
) {
    ActionButton(
        modifier = modifier,
        start = {
            MyIcon(
                MyIcons.hearth,
                null,
                modifier = Modifier.size(24.dp),
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
