package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun NavigateableItem(
    isSelected:Boolean,
    onClick:()->Unit,
    content:@Composable ()->Unit,
){
    val shape = RoundedCornerShape(12.dp)
    WithContentAlpha(if (isSelected)1f else 0.75f){
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .let {
                    if (isSelected) {
                        val selectionColor = myColors.onBackground
                        it
                            .border(
                                1.dp,
                                myColors.selectionGradient(0.10f, 0.05f, selectionColor),
                                shape
                            )
                            .background(myColors.selectionGradient(0.15f, 0f, selectionColor))
                    } else it
                }
                .clickable {
                    onClick()
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}