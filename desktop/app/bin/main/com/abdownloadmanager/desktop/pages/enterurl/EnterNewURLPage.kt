package com.abdownloadmanager.desktop.pages.enterurl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.desktop.window.custom.BaseOptionDialog
import com.abdownloadmanager.desktop.window.moveSafe
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.pages.enterurl.DownloaderSelection
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import java.awt.MouseInfo

@Composable
fun EnterNewURLPage(component: DesktopEnterNewURLComponent) {
    val linkFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        linkFocus.requestFocus()
        component.onPageOpen()
    }
    val text by component.url.collectAsState()
    Column {
        Column(
            Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
        ) {

            UrlTextField(
                text = text,
                setText = component::setURL,
                modifier = Modifier
                    .focusRequester(linkFocus)
                    .fillMaxWidth()
            )
        }
        Spacer(Modifier.weight(1f))
        Column {
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
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DownloaderSelectionSection(component)
                Spacer(Modifier.weight(1f))
                Actions(component)
            }
        }
    }

}

@Composable
private fun DownloaderSelectionSection(
    component: DesktopEnterNewURLComponent,
) {
    val downloaderSelection = component.downloaderSelection.collectAsState().value
    val bestDownloader = component.bestDownloader.collectAsState().value
    var isSelecting by remember { mutableStateOf(false) }
    val selectedName = rememberDownloaderSelectionItemString(
        downloaderSelection, bestDownloader
    )
    ActionButton(
        text = selectedName,
        end = {
            Row(
                Modifier.align(Alignment.CenterVertically)
            ) {
                Spacer(Modifier.width(4.dp))
                MyIcon(MyIcons.down, null, Modifier.size(12.dp))
            }
        },
        onClick = {
            isSelecting = !isSelecting
        }
    )

    if (isSelecting) {
        val state = rememberDialogState(
            size = DpSize.Unspecified,
        )
        BaseOptionDialog(
            onCloseRequest = {
                isSelecting = false
            },
            state = state,
            resizeable = false,
        ) {
            LaunchedEffect(window) {
                window.moveSafe(
                    MouseInfo.getPointerInfo().location.run {
                        DpOffset(
                            x = x.dp,
                            y = y.dp
                        )
                    }
                )
            }


            val shape = myShapes.defaultRounded
            Column(
                Modifier
                    .clip(shape)
                    .border(2.dp, myColors.onBackground / 10, shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                myColors.surface,
                                myColors.background,
                            )
                        )
                    )
            ) {
                WithContentColor(myColors.onBackground) {
                    Column(
                        Modifier
                            .widthIn(min = 100.dp, max = 300.dp)
                            .width(IntrinsicSize.Max)
                    ) {
                        component.possibleValues.onEach {
                            val text = rememberDownloaderSelectionItemString(
                                it,
                                bestDownloader,
                            )
                            Text(
                                text,
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        component.selectDownloader(it)
                                        isSelecting = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDownloaderSelectionItemString(
    downloaderSelection: DownloaderSelection,
    bestDownloader: TADownloaderInUI?,
): String {
    return when (downloaderSelection) {
        DownloaderSelection.Auto -> {
            val autoText = myStringResource(Res.string.auto)
            val bestDownloaderName = bestDownloader?.name?.rememberString()
            buildString {
                append(autoText)
                if (bestDownloader != null) {
                    append(" ($bestDownloaderName)")
                }
            }
        }

        is DownloaderSelection.Fixed -> {
            downloaderSelection.downloaderInUi.name.rememberString()
        }
    }
}

@Composable
private fun Actions(
    component: DesktopEnterNewURLComponent,
) {
    ActionButton(
        myStringResource(Res.string.ok),
        enabled = component.canAdd.collectAsState().value,
        onClick = {
            component.newDownloadEntered()
        }
    )
    Spacer(Modifier.width(8.dp))
    ActionButton(
        myStringResource(Res.string.cancel),
        onClick = component::close
    )
}

@Composable
private fun UrlTextField(
    text: String,
    setText: (String) -> Unit,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    MyTextFieldWithIcons(
        text,
        setText,
        myStringResource(Res.string.download_link),
        modifier = modifier.fillMaxWidth(),
        start = {
            MyIcon(
                MyIcons.link,
                null,
                Modifier.padding(horizontal = 8.dp)
                    .size(16.dp),
            )
        },
        end = {
            MyTextFieldIcon(MyIcons.paste) {
                setText(
                    ClipboardUtil.read()
                        .orEmpty()
                )
            }
        },
        errorText = errorText
    )
}
