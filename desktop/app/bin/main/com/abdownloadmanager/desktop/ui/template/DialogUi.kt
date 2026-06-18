package com.abdownloadmanager.desktop.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors

@Composable
fun DialogUi(
    mainContent: @Composable () -> Unit,
    footer: @Composable () -> Unit,
) {
    Column {
        Box(Modifier.weight(1f)) {
            mainContent()
        }
        footer()
    }
}

@Composable
fun DialogMainContent(
    contentPadding: PaddingValues = PaddingValues(
        top = 8.dp,
        bottom = 0.dp,
        start = 16.dp,
        end = 16.dp,
    ),
    content: @Composable () -> Unit,
) {
    Column(
        Modifier.padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun DialogFooter(
    content: @Composable () -> Unit,
) {
    Column {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
        ) {
            content()
        }
    }
}
