package com.abdownloadmanager.shared.util

import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import com.abdownloadmanager.shared.util.ui.widget.MPBackHandler
import ir.amirab.util.compose.modifiers.hijackClick
import ir.amirab.util.compose.modifiers.silentClickable
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Suppress("NAME_SHADOWING")
@Composable
fun ResponsiveDialog(
    state: ResponsiveDialogState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically { it },
    exit: ExitTransition = slideOutVertically { it },
    content: @Composable ResponsiveDialogScope.() -> Unit
) {

    // I don't know why but if I don't wrap these to rememberUpdateState
    // sometimes recomposition not works for lambda that placed in host
    val modifier by rememberUpdatedState(modifier)
    val onDismiss by rememberUpdatedState(onDismiss)
    val content by rememberUpdatedState(content)
    val enter by rememberUpdatedState(enter)
    val exit by rememberUpdatedState(exit)
    if (state.targetIsOpened || state.currentIsOpened) {
        PlaceInHost {
            val focusManager = LocalFocusManager.current
            LaunchedEffect(Unit) {
                focusManager.clearFocus()
            }
            CustomSheet(modifier, state, onDismiss, enter, exit, content)
        }
    }
}

@Stable
class ResponsiveDialogState(
    isOpened: Boolean
) {
    var targetIsOpened by mutableStateOf(isOpened)
        internal set
    var currentIsOpened by mutableStateOf(isOpened)
        internal set

    val isFullyVisible by derivedStateOf {
        val targetIsOpened = targetIsOpened
        val currentIsOpened = currentIsOpened
        targetIsOpened == currentIsOpened && targetIsOpened
    }

    val isFullyInvisible by derivedStateOf {
        val targetIsOpened = targetIsOpened
        val currentIsOpened = currentIsOpened
        targetIsOpened == currentIsOpened && !targetIsOpened
    }

    val onFullyInvisibleFlow = snapshotFlow { isFullyInvisible }
        .filter { it }.map { }

    val isIdle by derivedStateOf {
        targetIsOpened == currentIsOpened
    }

    fun show() {
        targetIsOpened = true
    }

    fun hide() {
        targetIsOpened = false
    }
}

@Composable
fun ResponsiveDialogState.OnFullyDismissed(
    onDismiss: () -> Unit,
) {
    val state = this
    val onDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(state) {
        val isFullyInvisibleAtFirst = state.isFullyInvisible
        val dropValue = if (isFullyInvisibleAtFirst) 1 else 0
        state.onFullyInvisibleFlow
            .drop(dropValue)
            .collect { onDismiss() }
    }
}

@Composable
fun rememberResponsiveDialogState(isOpened: Boolean): ResponsiveDialogState {
    return remember {
        ResponsiveDialogState(isOpened)
    }
}
interface ResponsiveDialogScope {
    val isTopEndFree: Boolean
    val isTopStartFree: Boolean
    val isBottomStartFree: Boolean
    val isBottomEndFree: Boolean
}

@Immutable
private data class ResponsiveDialogScopeImpl(
    override val isTopStartFree: Boolean,
    override val isTopEndFree: Boolean,
    override val isBottomStartFree: Boolean,
    override val isBottomEndFree: Boolean,
) : ResponsiveDialogScope

@Composable
private fun CustomSheet(
    modifier: Modifier,
    state: ResponsiveDialogState,
    onDismiss: () -> Unit,
    enter: EnterTransition = slideInVertically { it },
    exit: ExitTransition = slideOutVertically { it },
    content: @Composable (ResponsiveDialogScope.() -> Unit)
) {
    val originalTransition = updateTransition(state.targetIsOpened, "originalTransition")

    // it should be animated for the first time!
    var isVisible by remember { mutableStateOf(false) }
    val transition = updateTransition(isVisible, "transition")
    // the reason I use || is to prevent state from closing too early
    state.currentIsOpened = originalTransition.currentState || transition.currentState
    LaunchedEffect(originalTransition.targetState) {
        isVisible = originalTransition.targetState
    }
    val responsiveSize = rememberResponsiveWidth()
    Box(
        modifier.fillMaxSize(),
    ) {
        transition.AnimatedVisibility(
            visible = { it },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .silentClickable(onClick = onDismiss)
            )
        }
        val widthFraction: Float
        val alignment: Alignment
        when (responsiveSize) {
            ResponsiveTarget.Phone -> {
                widthFraction = 1f
                alignment = Alignment.BottomCenter
            }

            ResponsiveTarget.Tablet -> {
                widthFraction = 0.75f
                alignment = Alignment.Center
            }

            ResponsiveTarget.Desktop -> {
                widthFraction = 0.5f
                alignment = Alignment.Center
            }
        }

        val responsiveDialogScope = ResponsiveDialogScopeImpl(
            isTopStartFree = true,
            isTopEndFree = true,
            isBottomStartFree = alignment == Alignment.Center,
            isBottomEndFree = alignment == Alignment.Center,
        )

        transition.AnimatedVisibility(
            { it },
            enter = enter,
            exit = exit,
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .align(alignment)
                .hijackClick()
        ) {
            MPBackHandler(onBack = onDismiss)
            Box(Modifier) {
                content(responsiveDialogScope)
            }
        }
    }
}
