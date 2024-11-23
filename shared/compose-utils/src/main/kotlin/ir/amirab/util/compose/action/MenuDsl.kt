package ir.amirab.util.compose.action

import androidx.compose.ui.graphics.vector.ImageVector
import ir.amirab.util.compose.StringSource

@DslMarker
private annotation class MenuDsl

@MenuDsl
class MenuScope {
    private val list = mutableListOf<MenuItem>()
    fun item(
        title: StringSource,
        icon: ImageVector? = null,
        image: ImageVector? = null,
        onClick: AnAction.() -> Unit,
    ) {
        val action = simpleAction(
            title = title,
            icon = icon,
            image = image,
            onActionPerformed = onClick
        )
        list.add(action)
    }

    fun subMenu(
        title: StringSource,
        icon: ImageVector? = null,
        image: ImageVector? = null,
        block: MenuScope.() -> Unit,
    ) {
        val subMenu= MenuItem.SubMenu(
            title = title,
            icon = icon,
            image = image,
            items = MenuScope().apply(block).build()
        )
        list.add(subMenu)
    }

    fun separator() {
        MenuItem.Separator
            .let(list::add)
    }

    operator fun MenuItem.unaryPlus(){
        this.let(list::add)
    }

    fun build() = list.toList()
}

fun buildMenu(block: MenuScope.() -> Unit) = MenuScope().apply(block).build()
