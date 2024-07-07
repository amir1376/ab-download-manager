package com.abdownloadmanager.desktop.utils

import ir.amirab.util.FileUtils
import ir.amirab.util.flow.mapStateFlow
import com.abdownloadmanager.utils.isValidUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

sealed interface CanAddResult {
    data class DownloadAlreadyExists(val itemId: Long) : CanAddResult
    data object InvalidFileName : CanAddResult
    data object CantWriteInThisFolder : CanAddResult

    data object InvalidUrl : CanAddResult
    data object CanAdd : CanAddResult
}

//stateless class
class AddDownloadChecker(
    initialLink: String,
    initialName: String,
    initialFolder: String,
    private val downloadSystem: DownloadSystem,
    private val parentScope: CoroutineScope,
) {

    val canAddResult = MutableStateFlow(null as CanAddResult?)
    val canAdd = canAddResult.mapStateFlow() {
        it is CanAddResult.CanAdd
    }
    val isDuplicate = canAddResult.mapStateFlow() {
        it is CanAddResult.DownloadAlreadyExists
    }

    val link = MutableStateFlow(initialLink)
    val name = MutableStateFlow(initialName)
    val folder = MutableStateFlow(initialFolder)
    val length = MutableStateFlow(null as Long?)

    init {
        combine(
            this.link,
            this.name,
            this.folder,
            this.length,
            transform = { _ ->
                canAddResult.update { null }
            }
        ).launchIn(parentScope)
    }

    suspend fun check() {
        canAddResult.update { null }
        val newResult = validate()
        canAddResult.update { newResult }
    }

    private suspend fun validate(): CanAddResult {
        val link = this.link.value
        if (!isValidUrl(link)) {
            return CanAddResult.InvalidUrl
        }
        if (!fileNameValid()) {
            return CanAddResult.InvalidFileName
        }
        val name = name.value
        val folder = folder.value
        val items = downloadSystem
            .getDownloadItemByLink(link)
            .filter {
                it.name == name
            }

//        val fileExists = File(folder, name).exists()

        if (items.isNotEmpty()) {
            return CanAddResult.DownloadAlreadyExists(items.first().id)
        }
        if (!FileUtils.canWriteInThisFolder(folder)) {
            return CanAddResult.CantWriteInThisFolder
        }
//        if((length?:0)>File(folder).length()){
//            return  CanAddResult.NotEnoughMemory
//        }
        return CanAddResult.CanAdd
    }

    private fun fileNameValid(): Boolean {
        return FileNameValidator.isValidFileName(name.value)
    }


}
