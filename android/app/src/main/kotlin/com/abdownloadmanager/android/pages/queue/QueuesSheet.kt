package com.abdownloadmanager.android.pages.queue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.ui.configurable.RenderConfigurableGroup
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun QueueConfigSheet(
    queuesConfigurationComponent: QueueConfigurationComponent?,
    onDismiss: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(
        queuesConfigurationComponent
    ) {
        if (queuesConfigurationComponent != null) {
            state.show()
        } else {
            state.hide()
        }
    }
    state.OnFullyDismissed(onDismiss)
    ResponsiveDialog(state, onDismiss = state::hide) {
        queuesConfigurationComponent?.let {
            QueueConfig(
                name = it.downloadQueue.queueModel.collectAsState().value.name,
                groups = it.configurations,
                onDismissRequest = state::hide,
            )
        }
    }
}

@Composable
private fun ResponsiveDialogScope.QueueConfig(
    name: String,
    groups: List<ConfigurableGroup>,
    onDismissRequest: () -> Unit,
) {
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    val queues = myStringResource(Res.string.queues)
                    SheetTitle("${queues}: $name")
                }
            )
        }
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
        ) {
            for (group in groups) {
                RenderConfigurableGroup(
                    modifier = Modifier,
                    group = group,
                    itemPadding = PaddingValues(8.dp)
                )
            }
        }
    }
}
