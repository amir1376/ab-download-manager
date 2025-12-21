package com.abdownloadmanager.android.pages.enterurl

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.pages.enterurl.DownloaderSelection
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextFieldIcon
import com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun ResponsiveDialogScope.EnterNewURLPage(
    component: AndroidEnterNewURLComponent,
    onCloseRequest: () -> Unit,
) {
    val linkFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        linkFocus.requestFocus()
        component.onPageOpen()
    }
    val text by component.url.collectAsState()
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    SheetTitle(myStringResource(Res.string.new_download))
                },
                headerActions = {
                    DownloaderSelectionSection(component)
                    TransparentIconActionButton(
                        MyIcons.close,
                        contentDescription = Res.string.close.asStringSource(),
                        onClick = onCloseRequest,
                    )
                }
            )
        },
    ) {
        Column(
            Modifier
                .padding(horizontal = mySpacings.mediumSpace)
                .padding(bottom = mySpacings.mediumSpace)
        ) {
            UrlTextField(
                text = text,
                setText = component::setURL,
                modifier = Modifier
                    .focusRequester(linkFocus)
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(mySpacings.largeSpace))
            Actions(component, onCloseRequest)
        }
    }

}

@Composable
private fun DownloaderSelectionSection(
    component: AndroidEnterNewURLComponent,
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
        borderColor = SolidColor(Color.Transparent),
        onClick = {
            isSelecting = !isSelecting
        }
    )

    if (isSelecting) {
        Popup(
            onDismissRequest = {
                isSelecting = false
            },
        ) {
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
    component: AndroidEnterNewURLComponent,
    onCloseRequest: () -> Unit,
) {
    Row {
        ActionButton(
            myStringResource(Res.string.cancel),
            onClick = onCloseRequest,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        ActionButton(
            myStringResource(Res.string.ok),
            enabled = component.canAdd.collectAsState().value,
            onClick = {
                component.newDownloadEntered()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UrlTextField(
    text: String,
    setText: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
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
                Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp),
            )
        },
        end = {
            MyTextFieldIcon(
                icon = MyIcons.paste,
                onClick = {
                    setText(
                        ClipboardUtil.read()
                            .orEmpty()
                    )
                }
            )
        },
        errorText = errorText
    )
}
