package ir.amirab.util.compose.action

import ir.amirab.util.flow.mapStateFlow
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.*

sealed interface MenuItem {
    @Stable
    interface ReadableItem {
        //compose aware property
        val icon: StateFlow<ImageVector?>
        val image: StateFlow<ImageVector?>

        //compose aware property
        val title: StateFlow<StringSource>
    }

    interface CanBeModified {
        fun setIcon(icon: ImageVector?)
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
        icon: ImageVector? = null,
        image: ImageVector? = null,
    ) : MenuItem,
        ClickableItem,
        ReadableItem,
        CanBeModified,
        CanChangeEnabled,
            () -> Unit {
        var shouldDismissOnClick: Boolean = true


        private val _title: MutableStateFlow<StringSource> = MutableStateFlow(title)
        private val _icon: MutableStateFlow<ImageVector?> = MutableStateFlow(icon)
        private val _image: MutableStateFlow<ImageVector?> = MutableStateFlow(image)
        private val _isEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)

        override val title: StateFlow<StringSource> = _title.asStateFlow()
        override val icon: StateFlow<ImageVector?> = _icon.asStateFlow()
        override val image: StateFlow<ImageVector?> = _image.asStateFlow()
        override val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

        override fun setEnabled(boolean: Boolean) {
            _isEnabled.update { boolean }
        }

        override fun setIcon(icon: ImageVector?) {
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
        icon: ImageVector? = null,
        image: ImageVector? = null,
        title: StringSource,
        items: List<MenuItem>,
    ) : MenuItem,
        ReadableItem,
        HasEnable {
        private var _icon: MutableStateFlow<ImageVector?> = MutableStateFlow(icon)
        private var _image: MutableStateFlow<ImageVector?> = MutableStateFlow(image)
        private var _title: MutableStateFlow<StringSource> = MutableStateFlow(title)
        private val _items: MutableStateFlow<List<MenuItem>> = MutableStateFlow(items)

        override var icon: StateFlow<ImageVector?> = _icon.asStateFlow()
        override val image: StateFlow<ImageVector?> = _image.asStateFlow()
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

