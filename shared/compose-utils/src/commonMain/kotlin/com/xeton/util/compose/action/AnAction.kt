package com.xeton.util.compose.action

import com.xeton.util.compose.IconSource
import com.xeton.util.compose.StringSource

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


