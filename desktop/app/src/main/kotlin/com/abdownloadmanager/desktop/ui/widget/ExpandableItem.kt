package com.abdownloadmanager.desktop.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Composable
fun ExpandableItem(
    isExpanded:Boolean,
    header:@Composable ()->Unit,
    body:@Composable ()->Unit
){
    Column {
        header()
        AnimatedVisibility(isExpanded){
            body()
        }
    }
}