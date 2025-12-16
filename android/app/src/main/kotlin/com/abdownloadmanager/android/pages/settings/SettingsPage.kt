package com.abdownloadmanager.android.pages.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.configurable.RenderConfigurableGroup
import com.abdownloadmanager.shared.ui.widget.Handle
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.MultiplatformVerticalScrollbar
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.needScroll
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import io.github.oikvpqya.compose.fastscroller.ScrollbarAdapter
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

@Composable
private fun SideBarItem(icon: IconSource, name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(IntrinsicSize.Max)
            .ifThen(isSelected) {
                background(myColors.onBackground / 0.05f)
            }
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
    ) {
        Row(
            Modifier
                .padding(vertical = 8.dp)
                .padding(start = 16.dp)
                .padding(end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                MyIcon(
                    icon,
                    null,
                    Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    name,
                    Modifier.weight(1f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = myTextSizes.lg,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
        AnimatedVisibility(
            isSelected,
            modifier = Modifier
                .align(Alignment.CenterStart),
            enter = scaleIn(),
            exit = scaleOut(),
        ) {
            Spacer(
                Modifier
                    .height(16.dp)
                    .width(3.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 12.dp,
                            topEnd = 12.dp,
                        )
                    )
                    .background(myColors.primary)
            )
        }
        if (isSelected) {
            listOf(
                Alignment.TopCenter,
                Alignment.BottomCenter,
            ).forEach {
                Spacer(
                    Modifier
                        .align(it)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    myColors.onBackground / 0.1f,
                                    myColors.onBackground / 0.1f,
                                    Color.Transparent,
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun SettingsPage(
    settingsComponent: AndroidSettingsComponent,
) {
//    WindowTitle(myStringResource(Res.string.settings))
//    WindowIcon(MyIcons.settings)
//    WindowIcon(MyIcons.appIcon)
    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)
    val density = LocalDensity.current
    val systemBars = WindowInsets.systemBars
    val bottomPadding = systemBars.getBottom(density)
    val topPadding = systemBars.getTop(density)
    val topPaddingInDp = density.run {
        (topPadding).toDp()
    }
    val bottomPaddingInDp = density.run {
        (bottomPadding).toDp()
    }
    Box {
        Row(
            Modifier
                .fillMaxSize()
                .background(myColors.background)
        ) {
            VerticalScrollableContent(
                scrollState,
                Modifier
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                        .statusBarsPadding()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val configurableGroups by settingsComponent.configurables.collectAsState()
                    for (cfgGroup in configurableGroups) {
                        RenderConfigurableGroup(
                            cfgGroup,
                            Modifier,
                            itemPadding = PaddingValues(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            )
                        )
                    }
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(topPaddingInDp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            myColors.background,
                            Color.Transparent,
                        )
                    )
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomPaddingInDp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            myColors.background,
                        )
                    )
                )
        )
    }
}
