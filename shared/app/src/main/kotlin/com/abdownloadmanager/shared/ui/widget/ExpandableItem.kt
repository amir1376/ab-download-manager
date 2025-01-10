package com.abdownloadmanager.shared.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExpandableItem(
    isExpanded:Boolean,
    header:@Composable ()->Unit,
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier,
){
    Column(modifier) {
        header()
        AnimatedVisibility(isExpanded){
            body()
        }
    }
}