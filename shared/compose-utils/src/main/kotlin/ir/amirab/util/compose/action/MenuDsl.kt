package ir.amirab.util.compose.action

import ir.amirab.util.compose.IconSource

@DslMarker
private annotation class MenuDsl

@MenuDsl
class MenuScope {
    private val list = mutableListOf<MenuItem>()
    fun item(
        title: String,
        icon: IconSource? = null,
        onClick: AnAction.() -> Unit,
    ) {
        val action= simpleAction(title, icon, onClick)
        list.add(action)
    }

    fun subMenu(
        title: String,
        icon: IconSource? = null,
        block: MenuScope.() -> Unit,
    ) {
        val subMenu= MenuItem.SubMenu(
            title = title,
            icon = icon,
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
