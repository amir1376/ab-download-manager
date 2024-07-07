package com.abdownloadmanager.desktop.utils.action

import com.abdownloadmanager.desktop.ui.icon.IconSource
import com.abdownloadmanager.desktop.utils.asState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

inline fun simpleAction(
    title: String,
    icon: IconSource?=null,
    crossinline onActionPerformed: AnAction.() -> Unit
): AnAction {
    return object : AnAction(
        title = title, icon = icon,
    ) {
        override fun actionPerformed() = onActionPerformed()
    }
}
inline fun simpleAction(
    title: String,
    icon: IconSource?=null,
    checkEnable:StateFlow<Boolean>,
    crossinline onActionPerformed: AnAction.() -> Unit
): AnAction {
    return object : AnAction(
        title = title, icon = icon,
    ) {
        override val isEnabled: StateFlow<Boolean> = checkEnable
        override fun actionPerformed() = onActionPerformed()
    }
}