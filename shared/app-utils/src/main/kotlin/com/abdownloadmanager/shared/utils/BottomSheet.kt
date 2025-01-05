package com.abdownloadmanager.shared.utils

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Suppress("NAME_SHADOWING")
@Composable
fun ResponsiveDialog(
    modifier: Modifier = Modifier,
    isOpened: Boolean,
    onDismiss: () -> Unit,
    enter:EnterTransition=slideInVertically { it },
    exit:ExitTransition=slideOutVertically { it },
    content: @Composable ColumnScope.() -> Unit
) {

    // I don't know why but if I don't wrap these to rememberUpdateState
    // sometimes recomposition not works for lambda that placed in host
    val modifier by rememberUpdatedState(modifier)
    val isOpened by rememberUpdatedState(isOpened)
    val onDismiss by rememberUpdatedState(onDismiss)
    val content by rememberUpdatedState(content)
    val enter by rememberUpdatedState(enter)
    val exit by rememberUpdatedState(exit)
    PlaceInHost {
        CustomSheet(modifier, isOpened, onDismiss,enter,exit, content)
    }
}


@Composable
private fun CustomSheet(
    modifier: Modifier,
    isOpened: Boolean,
    onDismiss: () -> Unit,
    enter:EnterTransition=slideInVertically { it },
    exit:ExitTransition=slideOutVertically { it },
    content: @Composable (ColumnScope.() -> Unit)
) {
    val responsiveSize = rememberResponsiveWidth()
    Box(
        modifier.fillMaxSize(),
        contentAlignment = if (responsiveSize == ResponsiveTarget.Phone) Alignment.BottomCenter else Alignment.Center
    ) {
        AnimatedVisibility(
            isOpened,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
//            MPBackHandler(true, onDismiss)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }
        AnimatedVisibility(
            isOpened,
            enter = enter,
            exit = exit,
        ) {
//            MPBackHandler(enabled = isOpened, onBack = onDismiss)
            Column(Modifier.let {
                when (responsiveSize) {
                    ResponsiveTarget.Phone -> {
                        it.fillMaxWidth()
                    }

                    ResponsiveTarget.Tablet -> {
                        it.fillMaxWidth(0.7f)
                    }

                    else -> {
                        it.fillMaxWidth(0.5f)
                    }
                }
            }) {
                content()
            }
        }
    }
}
