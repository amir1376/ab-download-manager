package ir.amirab.util.compose.action

import ir.amirab.util.compose.IconSource

abstract class AnAction(
    title: String,
    icon: IconSource? = null,
) : MenuItem.SingleItem(
    title=title,
    icon=icon,
) {
    override fun onClick() = actionPerformed()

    abstract fun actionPerformed()
}


