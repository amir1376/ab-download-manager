package ir.amirab.util.compose.action

import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.*

inline fun simpleAction(
    title: StringSource,
    icon: IconSource? = null,
    crossinline onActionPerformed: AnAction.() -> Unit,
): AnAction {
    return object : AnAction(
        title = title, icon = icon,
    ) {
        override fun actionPerformed() = onActionPerformed()
    }
}

inline fun simpleAction(
    title: StringSource,
    icon: IconSource? = null,
    checkEnable: StateFlow<Boolean>,
    crossinline onActionPerformed: AnAction.() -> Unit,
): AnAction {
    return object : AnAction(
        title = title, icon = icon,
    ) {
        override val isEnabled: StateFlow<Boolean> = checkEnable
        override fun actionPerformed() = onActionPerformed()
    }
}

inline fun toggleableAction(
    title: StringSource,
    icon: IconSource? = null,
    isToggled: StateFlow<Boolean>,
    crossinline onActionPerformed: AToggleableAction.() -> Unit,
): AToggleableAction {
    return object : AToggleableAction(
        title = title, icon = icon,
    ) {
        override fun actionPerformed() = onActionPerformed()
        override val isToggled: StateFlow<Boolean> = isToggled
    }
}

inline fun toggleableAction(
    title: StringSource,
    icon: IconSource? = null,
    checkEnable: StateFlow<Boolean>,
    isToggled: StateFlow<Boolean>,
    crossinline onActionPerformed: AToggleableAction.() -> Unit,
): AToggleableAction {
    return object : AToggleableAction(
        title = title, icon = icon,
    ) {
        override val isEnabled: StateFlow<Boolean> = checkEnable
        override fun actionPerformed() = onActionPerformed()
        override val isToggled: StateFlow<Boolean> = isToggled
    }
}
