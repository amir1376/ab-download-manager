package com.abdownloadmanager.android.pages.crashreport

import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.ScreenSurface
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.util.ClipboardUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.android.util.AppInfo
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun ErrorWindow(
    throwable: ThrowableData,
    close: () -> Unit,
) {
    val state = rememberResponsiveDialogState(true)
    state.OnFullyDismissed(close)
    ResponsiveDialog(state, state::hide) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle("Application Crash")
                    }
                )
            }
        ) {
            ErrorUi(throwable, state::hide)
        }
    }
}

@Composable
private fun ErrorUi(
    e: ThrowableData,
    close: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = mySpacings.largeSpace)
            .padding(bottom = mySpacings.largeSpace),
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
                .heightIn(max = 380.dp)
                .weight(1f, false),
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

fun createInformation(
    exceptionString: ThrowableData,
): String {

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
private fun Header(modifier: Modifier = Modifier, e: ThrowableData) {
    Text(
        text = "We got an error in the application (\"${e.title}\")", modifier = modifier,
        fontSize = myTextSizes.xl
    )
}

@Composable
private fun RenderException(modifier: Modifier, e: ThrowableData) {
    val errorText = e.stacktrace
    Box(
        modifier = modifier
            .background(myColors.background)
            .clip(myShapes.defaultRounded)
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        SelectionContainer {
            Text(
                text = errorText,
                color = myColors.error,
                fontSize = myTextSizes.base,
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
            text = myStringResource(Res.string.close),
            onClick = close,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(mySpacings.mediumSpace))
        ActionButton(
            text = myStringResource(Res.string.copy),
            onClick = copyInformation,
            modifier = Modifier.weight(1f)
        )
    }
}
