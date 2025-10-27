package com.abdownloadmanager.android.ui.configurable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import ir.amirab.util.compose.StringSource

@Composable
fun ConfigurableSheet(
    title: StringSource,
    isOpened: Boolean,
    onDismiss: () -> Unit,
    headerActions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val dialogState = rememberResponsiveDialogState(isOpened)
    LaunchedEffect(isOpened) {
        when (isOpened) {
            true -> dialogState.show()
            false -> dialogState.hide()
        }
    }
    dialogState.OnFullyDismissed {
        onDismiss()
    }
    ResponsiveDialog(
        state = dialogState,
        onDismiss = dialogState::hide,
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            title.rememberString()
                        )
                    },
                    headerActions = headerActions,
                )
            }
        ) {
            content()
        }
    }
}
