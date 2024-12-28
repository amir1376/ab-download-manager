package ir.amirab.util.compose.action

import ir.amirab.util.compose.IconSource
import ir.amirab.util.flow.mapStateFlow
import androidx.compose.runtime.*
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.*

sealed interface MenuItem {
    @Stable
    interface ReadableItem {
        //compose aware property
        val icon: StateFlow<IconSource?>

        //compose aware property
        val title: StateFlow<StringSource>
    }

    interface CanBeModified {
        fun setIcon(icon: IconSource?)
        fun setTitle(title: StringSource)
    }

    interface HasEnable {
        //compose aware property
        val isEnabled: StateFlow<Boolean>
    }

    interface CanChangeEnabled {
        fun setEnabled(boolean: Boolean)
    }

    interface ClickableItem : HasEnable {
        fun onClick()
    }

    abstract class SingleItem(
        title: StringSource,
        icon: IconSource? = null,
    ) : MenuItem,
        ClickableItem,
        ReadableItem,
        CanBeModified,
        CanChangeEnabled,
            () -> Unit {
        var shouldDismissOnClick: Boolean = true


        private val _title: MutableStateFlow<StringSource> = MutableStateFlow(title)
        private val _icon: MutableStateFlow<IconSource?> = MutableStateFlow(icon)
        private val _isEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)

        override val title: StateFlow<StringSource> = _title.asStateFlow()
        override val icon: StateFlow<IconSource?> = _icon.asStateFlow()
        override val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

        override fun setEnabled(boolean: Boolean) {
            _isEnabled.update { boolean }
        }

        override fun setIcon(icon: IconSource?) {
            _icon.update { icon }
        }

        override fun setTitle(title: StringSource) {
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
        title: StringSource,
        items: List<MenuItem>,
    ) : MenuItem,
        ReadableItem,
        HasEnable {
        private var _icon: MutableStateFlow<IconSource?> = MutableStateFlow(icon)
        private var _title: MutableStateFlow<StringSource> = MutableStateFlow(title)
        private val _items: MutableStateFlow<List<MenuItem>> = MutableStateFlow(items)

        override var icon: StateFlow<IconSource?> = _icon.asStateFlow()
        override var title: StateFlow<StringSource> = _title.asStateFlow()

        val items: StateFlow<List<MenuItem>> = _items.asStateFlow()
        fun setItems(newItems: List<MenuItem>) {
            _items.update { newItems }
        }

        override val isEnabled: StateFlow<Boolean> = this.items.mapStateFlow {
            it.isNotEmpty()
        }
    }

    data object Separator : MenuItem
}

