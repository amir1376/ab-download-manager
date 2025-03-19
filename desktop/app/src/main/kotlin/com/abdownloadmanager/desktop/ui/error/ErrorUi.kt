package com.abdownloadmanager.desktop.ui.error

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.widget.ScreenSurface
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun ErrorWindow(
    throwable: Throwable,
    close: () -> Unit,
){
    CustomWindow(
        onCloseRequest = close,
        resizable = true,
        state = rememberWindowState(
            size = DpSize(500.dp,400.dp),
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        alwaysOnTop = true,
    ) {
        ErrorUi(throwable, close)
    }
}

@Composable
private fun ErrorUi(
    e: Throwable,
    close: () -> Unit,
) {
    WindowTitle("Error")
    ScreenSurface(
        modifier = Modifier.fillMaxSize(),
        contentColor = myColors.onBackground,
        background = myColors.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Header(
                modifier = Modifier
                    .fillMaxWidth(),
                e
            )
            Spacer(Modifier.height(8.dp))
            RenderException(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                e = e
            )
            Spacer(Modifier.height(8.dp))
            Actions(
                modifier = Modifier
                    .fillMaxWidth(),
                close = close,
                copyInformation = {
                    ClipboardUtil.copy(createInformation(e))
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

fun createInformation(
    e: Throwable,
): String {
    val exceptionString = e.stackTraceToString().replace("\t", "    ")
    val version = AppInfo.version
    val platform = AppInfo.platform.name
    return """
### Application Runtime Error
###### App Info
```
appVersion = $version
platform = $platform
```
###### Exception
```
$exceptionString
```
""".trimIndent()
}

@Composable
private fun Header(modifier: Modifier = Modifier, e: Throwable) {
    Text(
        text = "There is an error happen in application (\"${e.localizedMessage}\")", modifier = modifier,
        fontSize = myTextSizes.xl
    )
}

@Composable
private fun RenderException(modifier: Modifier, e: Throwable) {
    val errorText = remember(e) {
        e.stackTraceToString()
            //replace tab with space for compose to render it correctly
            .replace("\t", "    ")
    }
    Box(
        modifier = modifier
            .background(myColors.surface)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        SelectionContainer {
            Text(
                text = errorText,
                color = myColors.error,
                fontSize = myTextSizes.xl,
            )
        }
    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    close: () -> Unit,
    copyInformation: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
    ) {
        ActionButton(
            text = "Copy Information",
            onClick = copyInformation
        )
        Spacer(Modifier.width(8.dp))
        ActionButton(
            text = "Close",
            onClick = close
        )
    }
}
