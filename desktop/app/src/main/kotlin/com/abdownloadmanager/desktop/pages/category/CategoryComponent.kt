package com.abdownloadmanager.desktop.pages.category

import arrow.core.split
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.utils.BaseComponent
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.category.CategoryManager
import com.abdownloadmanager.utils.category.iconSource
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.uriOrNull
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class CategoryComponent(
    ctx: ComponentContext,
    val id: Long,
    val close: () -> Unit,
    private val submit: (Category) -> Unit,
) : BaseComponent(ctx), KoinComponent {
    private val appRepository: AppRepository by inject()
    val defaultDownloadLocation = appRepository.saveLocation
    private val categoryManager: CategoryManager by inject()

    init {
        if (id >= 0) {
            loadCategoryData()
        }
    }

    fun loadCategoryData() {
        scope.launch {
            val category = categoryManager.getCategoryById(id) ?: return@launch
            setIcon(category.iconSource())
            setName(category.name)
            setTypes(category.acceptedFileTypes.joinToString(" "))
            setPath(category.path)
        }
    }

    private val _icon = MutableStateFlow(null as IconSource?)
    val icon = _icon.asStateFlow()
    fun setIcon(iconSource: IconSource?) {
        _icon.value = iconSource
    }

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()
    fun setName(name: String) {
        _name.value = name
    }

    private val _types = MutableStateFlow("")
    val types = _types.asStateFlow()
    fun setTypes(types: String) {
        _types.value = types
    }

    private val _path = MutableStateFlow("")
    val path = _path.asStateFlow()
    fun setPath(path: String) {
        _path.value = path
    }

    val canSubmit = combineStateFlows(
        icon,
        name,
        types,
        path
    ) { icon, name, types, path ->
        val iconOk = icon != null
        val nameOk = name.isNotBlank()
        val pathOk = FileUtils.canWriteInThisFolder(path)
        iconOk && nameOk && pathOk
    }
    val isEditMode = id >= 0

    fun submit() {
        if (!canSubmit.value) {
            return
        }
        val path = path.value
        kotlin.runCatching {
            File(path).mkdirs()
        }
        submit(
            Category(
                id = id,
                name = name.value,
                acceptedFileTypes = types.value
                    .split(" ")
                    .filterNot { it.isBlank() }
                    .distinct(),
                icon = icon
                    .value!!
                    .uriOrNull()!!,
                path = path,
                items = emptyList() // ignored!
            )
        )
    }
}