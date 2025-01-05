package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.div

@Composable
fun ActionContainer(
    modifier: Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 8.dp,
    ),
    content: @Composable () -> Unit,
) {
    Column(modifier) {
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
                .padding(contentPadding),
        ) {
            content()
        }
    }
}