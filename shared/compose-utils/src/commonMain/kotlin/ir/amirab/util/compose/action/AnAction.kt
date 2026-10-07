package ir.amirab.util.compose.action

import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource

abstract class AnAction(
    title: StringSource,
    icon: IconSource? = null,
) : MenuItem.SingleItem(
    title = title,
    icon = icon,
) {
    override fun onClick() = actionPerformed()

    abstract fun actionPerformed()
}

abstract class AToggleableAction(
    title: StringSource,
    icon: IconSource? = null,
) : MenuItem.ToggleableItem(
    title = title,
    icon = icon,
) {
    override fun onClick() = actionPerformed()

    abstract fun actionPerformed()
}


