package com.abdownloadmanager.shared.pages.category

import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.iconSource
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.IconSource
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
    private val appRepository: BaseAppRepository by inject()
    val defaultDownloadLocation = appRepository.saveLocation
    private val categoryManager: CategoryManager by inject()
    private val iconResolver: IIconResolver by inject()

    init {
        if (id >= 0) {
            loadCategoryData()
        }
    }

    fun loadCategoryData() {
        scope.launch {
            val category = categoryManager.getCategoryById(id) ?: return@launch
            setIcon(category.iconSource(iconResolver))
            setName(category.name)
            setTypesEnabled(category.acceptedFileTypes.isNotEmpty())
            setTypes(category.acceptedFileTypes.joinToString(" "))
            setUrlPatternsEnabled(category.acceptedUrlPatterns.isNotEmpty())
            setUrlPatterns(category.acceptedUrlPatterns.joinToString(" "))
            setPath(category.path)
            setUsePath(category.usePath)
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

    private val _typesEnabled = MutableStateFlow(false)
    val typesEnabled = _typesEnabled.asStateFlow()
    fun setTypesEnabled(value: Boolean) {
        _typesEnabled.value = value
    }

    private val _types = MutableStateFlow("")
    val types = _types.asStateFlow()
    fun setTypes(types: String) {
        _types.value = types
    }

    private val _urlPatternsEnabled = MutableStateFlow(false)
    val urlPatternsEnabled = _urlPatternsEnabled.asStateFlow()
    fun setUrlPatternsEnabled(urlPatterns: Boolean) {
        _urlPatternsEnabled.value = urlPatterns
    }

    private val _urlPatterns = MutableStateFlow("")
    val urlPatterns = _urlPatterns.asStateFlow()
    fun setUrlPatterns(urlPatterns: String) {
        _urlPatterns.value = urlPatterns
    }

    private val _path = MutableStateFlow("")
    val path = _path.asStateFlow()
    fun setPath(path: String) {
        _path.value = path
    }

    private val _usePath = MutableStateFlow(false)
    val usePath = _usePath.asStateFlow()
    fun setUsePath(usePath: Boolean) {
        _usePath.value = usePath
    }

    val canSubmit = combineStateFlows(
        icon,
        name,
        types,
        path,
        usePath,
    ) { icon, name, types, path, usePath ->
        val iconOk = icon != null
        val nameOk = name.isNotBlank()
        val pathOk = FileUtils.Companion.canWriteInThisFolder(path) || !usePath
        iconOk && nameOk && pathOk
    }
    val isEditMode = id >= 0

    fun submit() {
        if (!canSubmit.value) {
            return
        }
        val path = path.value
        runCatching {
            File(path).mkdirs()
        }
        submit(
            Category(
                id = id,
                name = name.value,
                acceptedFileTypes = if (typesEnabled.value) {
                    types.value
                        .split(" ")
                        .filterNot { it.isBlank() }
                        .distinct()
                } else {
                    emptyList()
                },
                icon = icon
                    .value!!
                    .uri!!,
                path = path,
                usePath = usePath.value,
                acceptedUrlPatterns = if (urlPatternsEnabled.value) {
                    urlPatterns.value
                        .split(" ")
                        .filterNot { it.isBlank() }
                        .distinct()
                } else {
                    emptyList()
                },
                items = emptyList() // ignored!
            )
        )
    }
}
