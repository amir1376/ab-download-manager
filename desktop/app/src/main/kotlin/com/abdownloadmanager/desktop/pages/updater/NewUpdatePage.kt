package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.ui.customwindow.WindowIcon
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.colored.AppIcon
import io.github.z4kn4fein.semver.Version
import com.abdownloadmanager.updatechecker.VersionData

@Composable
fun NewUpdatePage(
    versionVersionData: VersionData,
    currentVersion: Version,
    update: () -> Unit,
    cancel: () -> Unit,
) {
    WindowTitle("New Update")
    WindowIcon(icon = AbIcons.Colored.AppIcon)
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(
                bottom = 16.dp,
                top = 8.dp
            )
    ) {
        Text(
            text = "There is a new version of app is available",
            fontSize = myTextSizes.xl,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        WithContentAlpha(0.75f){
            Text(
                text = "you can press on update button to update to the latest version",
                fontSize = myTextSizes.base,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row {
            RenderKeyValue("Current Version", currentVersion.toString())
            Spacer(Modifier.width(16.dp))
            RenderKeyValue("Latest Version", versionVersionData.version.toString())
        }
        Spacer(Modifier.height(8.dp))
        RenderChangeLog(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            versionVersionData.changeLog
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
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
        text = "Update",
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
        text = "Cancel",
        modifier = modifier,
        onClick = cancel,
    )
}

@Composable
fun RenderChangeLog(modifier: Modifier, changeLog: String) {
    Column(modifier) {
        Text(
            text = "Changelog",
            fontSize = myTextSizes.base,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(myColors.onBackground / 5)
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            SelectionContainer {
                WithContentAlpha(0.75f) {
                    Text(
                        text = changeLog,
                        fontSize = myTextSizes.base,
                    )
                }
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
            Text(key, fontSize = myTextSizes.base)
        }
        Spacer(Modifier.width(8.dp))
        Text(value, fontSize = myTextSizes.base)
    }
}