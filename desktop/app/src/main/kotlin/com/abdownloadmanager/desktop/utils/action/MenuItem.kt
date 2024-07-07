package com.abdownloadmanager.desktop.utils.action

import com.abdownloadmanager.desktop.ui.icon.IconSource
import ir.amirab.util.flow.mapStateFlow
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.*

sealed interface MenuItem {
    @Stable
    interface ReadableItem {
        //compose aware property
        val icon: StateFlow<IconSource?>

        //compose aware property
        val title: StateFlow<String>
    }

    interface CanBeModified{
        fun setIcon(icon:IconSource?)
        fun setTitle(title:String)
    }

    interface HasEnable {
        //compose aware property
        val isEnabled: StateFlow<Boolean>
    }
    interface CanChangeEnabled{
        fun setEnabled(boolean: Boolean)
    }

    interface ClickableItem : HasEnable {
        fun onClick()
    }

    abstract class SingleItem(
        title: String,
        icon: IconSource?=null,
    ) : MenuItem,
        ClickableItem,
        ReadableItem,
        CanBeModified,
        CanChangeEnabled,
            () -> Unit {
        var shouldDismissOnClick: Boolean = true



        private val _title: MutableStateFlow<String> = MutableStateFlow(title)
        private val _icon: MutableStateFlow<IconSource?> = MutableStateFlow(icon)
        private val _isEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)

        override val title: StateFlow<String> = _title.asStateFlow()
        override val icon: StateFlow<IconSource?> = MutableStateFlow(icon)
        override val isEnabled: StateFlow<Boolean> = MutableStateFlow(true)

        override fun setEnabled(boolean: Boolean) {
            _isEnabled.update { boolean }
        }

        override fun setIcon(icon: IconSource?) {
            _icon.update { icon }
        }

        override fun setTitle(title: String) {
            _title.update { title }
        }

        final override fun invoke() {
            if (isEnabled.value) {
                onClick()
            }
        }

        abstract override fun onClick()
    }

    class SubMenu(
        icon: IconSource? = null,
        title: String,
        items: List<MenuItem>,
    ) : MenuItem,
        ReadableItem,
        HasEnable {
        private var _icon: MutableStateFlow<IconSource?> = MutableStateFlow(icon)
        private var _title: MutableStateFlow<String> = MutableStateFlow(title)
        private val _items: MutableStateFlow<List<MenuItem>> = MutableStateFlow(items)

        override var icon: StateFlow<IconSource?> = _icon.asStateFlow()
        override var title: StateFlow<String> = _title.asStateFlow()

        val items:StateFlow<List<MenuItem>> = _items.asStateFlow()
        fun setItems(newItems:List<MenuItem>){
            _items.update { newItems }
        }

        override val isEnabled: StateFlow<Boolean> = this.items.mapStateFlow {
            it.isNotEmpty()
        }
    }

    data object Separator : MenuItem
}

