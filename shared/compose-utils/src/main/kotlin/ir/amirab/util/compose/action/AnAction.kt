package ir.amirab.util.compose.action

import androidx.compose.ui.graphics.vector.ImageVector
import ir.amirab.util.compose.StringSource

abstract class AnAction(
    title: StringSource,
    icon: ImageVector? = null,
    image: ImageVector? = null,
) : MenuItem.SingleItem(
    title=title,
    icon=icon,
    image=image,
) {
    override fun onClick() = actionPerformed()

    abstract fun actionPerformed()
}


