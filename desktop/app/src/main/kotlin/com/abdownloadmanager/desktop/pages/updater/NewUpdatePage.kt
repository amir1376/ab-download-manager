package com.abdownloadmanager.desktop.pages.updater

import androidx.compose.foundation.*
import com.abdownloadmanager.desktop.ui.customwindow.WindowIcon
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.theme.myMarkdownColors
import com.abdownloadmanager.desktop.ui.theme.myMarkdownTypography
import com.abdownloadmanager.resources.Res
import io.github.z4kn4fein.semver.Version
import com.abdownloadmanager.updatechecker.UpdateInfo
import com.abdownloadmanager.utils.compose.needScroll
import com.mikepenz.markdown.compose.Markdown
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun NewUpdatePage(
    newVersionInfo: UpdateInfo,
    currentVersion: Version,
    update: () -> Unit,
    cancel: () -> Unit,
) {
    WindowTitle(myStringResource(Res.string.update_updater))
    WindowIcon(MyIcons.refresh)
    Box {
        BackgroundEffects()
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(
                        bottom = 16.dp,
                        top = 8.dp
                    )
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = myStringResource(Res.string.update_available),
                        fontSize = myTextSizes.xl,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = myStringResource(
                            Res.string.version_n, Res.string.version_n_createArgs(
                                newVersionInfo.version.toString()
                            )
                        ),
                        fontSize = myTextSizes.xl,
                        fontWeight = FontWeight.Bold,
                        color = myColors.success,
                    )
                }
                Spacer(Modifier.height(8.dp))
                WithContentAlpha(0.8f) {
                    Text(
                        text = myStringResource(Res.string.update_available_suggest_to_to_update),
                        fontSize = myTextSizes.base,
                    )
                }
                Spacer(Modifier.height(8.dp))
                RenderChangeLog(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    newVersionInfo.changeLog
                )
            }
            Actions(
                Modifier.fillMaxWidth(),
                update,
                cancel
            )
        }
    }
}

@Composable
private fun BoxScope.BackgroundEffects() {
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .offset(y = (-148).dp)
            .fillMaxWidth(0.5f)
            .height(200.dp)
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
            .align(Alignment.BottomEnd)
            .size(180.dp)
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
fun Actions(modifier: Modifier, update: () -> Unit, cancel: () -> Unit) {
    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            UpdateButton(Modifier, update)
            Spacer(Modifier.width(8.dp))
            CancelButton(Modifier, cancel)
        }
    }
}

@Composable
fun UpdateButton(
    modifier: Modifier,
    update: () -> Unit,
) {
    val backgroundColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 30
        }
    )
    val borderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors
    )
    val disabledBorderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 50
        }
    )
    ActionButton(
        text = myStringResource(Res.string.update),
        modifier = modifier,
        onClick = update,
        backgroundColor = backgroundColor,
        disabledBackgroundColor = backgroundColor,
        borderColor = borderColor,
        disabledBorderColor = disabledBorderColor,
    )
}

@Composable
fun CancelButton(
    modifier: Modifier,
    cancel: () -> Unit,
) {
    ActionButton(
        text = myStringResource(Res.string.cancel),
        modifier = modifier,
        onClick = cancel,
    )
}

@Composable
private fun RenderChangeLog(modifier: Modifier, changeLog: String) {
    val trimmedChangelog = remember {
        changeLog
            .lines()
            .filterNot { it.isBlank() }
            .joinToString("\n")
    }
    Column(modifier) {
        Text(
            text = myStringResource(Res.string.update_release_notes),
            fontWeight = FontWeight.Bold,
            fontSize = myTextSizes.lg,
        )
        Spacer(Modifier.height(8.dp))
        val shape = RoundedCornerShape(6.dp)
        val scrollState = rememberScrollState()
        val scrollbarAdapter = rememberScrollbarAdapter(scrollState)
        Row(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .border(1.dp, myColors.onBackground / 0.05f, shape)
                .background(myColors.surface / 75)
        ) {
            Markdown(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(8.dp),
                content = trimmedChangelog,
                colors = myMarkdownColors(),
                typography = myMarkdownTypography()
            )
            if (scrollbarAdapter.needScroll()) {
                VerticalScrollbar(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            vertical = 4.dp,
                            horizontal = 4.dp
                        ),
                    style = LocalScrollbarStyle.current.copy(
                        thickness = 8.dp
                    ),
                    adapter = scrollbarAdapter
                )
            }
        }
    }
}

@Composable
private fun RenderKeyValue(
    key: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WithContentAlpha(0.50f) {
            Text(
                key,
                fontSize = myTextSizes.base,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            fontSize = myTextSizes.base,
            maxLines = 1,
        )
    }
}