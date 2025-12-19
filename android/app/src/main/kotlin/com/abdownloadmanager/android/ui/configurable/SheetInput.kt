package com.abdownloadmanager.android.ui.configurable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.resources.myStringResource

@Immutable
data class InputParams<T>(
    val editingValue: T,
    val setEditingValue: (T) -> Unit,
    val modifier: Modifier,
    val keyboardActions: KeyboardActions,
)

@Composable
fun <T> SheetInput(
    configurable: Configurable<T>,
    isOpened: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit,
    inputContent: @Composable (InputParams<T>) -> Unit,
) {
    SheetInput(
        title = configurable.title,
        validate = configurable.validate,
        isOpened = isOpened,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        inputContent = inputContent,
        initialValue = { configurable.stateFlow.value },
    )
}

@Composable
fun <T> SheetInput(
    title: StringSource,
    validate: (T) -> Boolean,
    isOpened: Boolean,
    initialValue: () -> T,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit,
    inputContent: @Composable (InputParams<T>) -> Unit,
) {
    ConfigurableSheet(
        title = title,
        onDismiss = onDismiss,
        isOpened = isOpened,
        headerActions = {
            TransparentIconActionButton(
                MyIcons.close,
                contentDescription = myStringResource(Res.string.close),
                onClick = onDismiss
            )
        }
    ) {
        Column(
            Modifier.padding(horizontal = mySpacings.mediumSpace)
        ) {
            var editingValue by remember(initialValue) {
                mutableStateOf(initialValue())
            }
            val isInputValid = remember(validate, editingValue) {
                validate(editingValue)
            }
            val fr = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                fr.requestFocus()
            }
            inputContent(
                InputParams(
                    editingValue = editingValue,
                    setEditingValue = {
                        editingValue = it
                    },
                    modifier = Modifier
                        .focusRequester(fr),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isInputValid) {
                                onConfirm(editingValue)
                            }
                        },
                    )
                )
            )

            Spacer(Modifier.height(mySpacings.mediumSpace))
            Row {
                ActionButton(
                    text = myStringResource(Res.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(mySpacings.mediumSpace))
                ActionButton(
                    text = myStringResource(Res.string.ok),
                    onClick = {
                        onConfirm(editingValue)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isInputValid,
                )
            }
            Spacer(Modifier.height(mySpacings.mediumSpace))
        }
    }
}
