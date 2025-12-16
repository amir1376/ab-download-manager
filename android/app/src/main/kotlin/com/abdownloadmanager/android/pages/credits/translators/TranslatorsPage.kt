package com.abdownloadmanager.android.pages.credits.translators

import com.abdownloadmanager.android.di.Di
import com.abdownloadmanager.resources.ABDMResources
import com.abdownloadmanager.shared.ui.widget.MaybeLinkText
import kotlinx.coroutines.runBlocking

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.credits.translators.LanguageTranslationInfo
import com.abdownloadmanager.shared.pages.credits.translators.TranslatorData
import com.abdownloadmanager.shared.ui.widget.PrimaryMainActionButton
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.URLOpener
import ir.amirab.util.compose.localizationmanager.LanguageNameProvider
import ir.amirab.util.compose.localizationmanager.MyLocale
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlinx.serialization.json.Json
import org.koin.core.component.get

@Composable
fun TranslatorsPage(onBack: () -> Unit) {
    Translators(
        Modifier
            .fillMaxSize()
            .background(myColors.background)
    )
}

@Composable
internal fun Translators(modifier: Modifier) {
    Column(
        modifier
    ) {
        DearTranslators(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .weight(1f)
        )
        ContributionNotice(
            modifier = Modifier,
            onUserWantsToContribute = {
                URLOpener.openUrl(SharedConstants.projectTranslations)
            }
        )
    }
}

@Composable
private fun ContributionNotice(
    modifier: Modifier,
    onUserWantsToContribute: () -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(myColors.surface),
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onSurface / 0.15f)
        )
        Column(
            Modifier
                .padding(mySpacings.largeSpace)
                .navigationBarsPadding()
        ) {
            Text(
                myStringResource(Res.string.translators_page_thanks),
                modifier = Modifier,
                fontSize = myTextSizes.lg,
                fontWeight = FontWeight.Bold,
            )
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(myColors.surface)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(
                        myStringResource(Res.string.translators_contribute_title),
                        fontSize = myTextSizes.lg,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        myStringResource(Res.string.translators_contribute_description),
                        fontSize = myTextSizes.base,
                        color = LocalContentColor.current / 0.75f
                    )
                }
            }
            Spacer(Modifier.height(mySpacings.largeSpace))
            PrimaryMainActionButton(
                text = myStringResource(Res.string.contribute),
                onClick = onUserWantsToContribute,
                modifier = Modifier.fillMaxWidth(),
                enabled = true,
            )
        }
    }
}

@Composable
private fun DearTranslators(
    modifier: Modifier,
) {
    val itemHorizontalPadding = 16.dp
    val list = rememberLanguageTranslationInfo()

    val state = rememberLazyListState()

    LazyColumn(
        modifier,
        state = state
    ) {
        itemsIndexed(list) { index, item ->
            TranslatedLanguageItem(
                item,
                Modifier
                    .fillMaxWidth()
                    .ifThen(index % 2 == 1) {
                        background(myColors.surface)
                    }
                    .padding(16.dp, itemHorizontalPadding)
            )
        }
    }
}

@Composable
private fun TranslatedLanguageItem(
    translationInfo: LanguageTranslationInfo,
    modifier: Modifier,
) {
    Column(modifier) {
        Column {
            WithContentAlpha(1f) {
                Text(
                    translationInfo.nativeName,
                    fontSize = myTextSizes.base,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(mySpacings.smallSpace))
            WithContentAlpha(0.75f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        translationInfo.englishName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = myTextSizes.base,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        translationInfo.locale,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = myTextSizes.base,
                        color = myColors.primary,
                        modifier = Modifier
                            .background(myColors.primary / 10)
                            .padding(vertical = 0.dp, horizontal = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(mySpacings.mediumSpace))
        Column(
            verticalArrangement = Arrangement.spacedBy(mySpacings.smallSpace)
        ) {
            translationInfo.translators.forEach {
                MaybeLinkText(
                    it.name,
                    it.link,
                )
            }
        }
    }
}

private fun convertLanguageToMyLocale(language: String): MyLocale {
    return language.split("-").run {
        MyLocale(
            languageCode = get(0),
            countryCode = getOrNull(1)
        )
    }
}

@Composable
private fun rememberLanguageTranslationInfo(): List<LanguageTranslationInfo> {
    return remember {
        val json = Di.get<Json>()
        val translatorData = runBlocking {
            ABDMResources.getTranslatorsContent()
        }.let {
            json.decodeFromString<TranslatorData>(it)
        }
        translatorData.map {
            val name = LanguageNameProvider.getName(convertLanguageToMyLocale(it.key))
            LanguageTranslationInfo(
                locale = it.key,
                englishName = name.englishName,
                nativeName = name.nativeName,
                translators = it.value,
            )
        }
    }
}
