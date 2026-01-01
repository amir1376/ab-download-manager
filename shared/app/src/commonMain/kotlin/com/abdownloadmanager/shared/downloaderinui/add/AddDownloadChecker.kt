package com.abdownloadmanager.shared.downloaderinui.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.utils.DuplicateFilterByPath
import ir.amirab.util.FileNameValidator
import ir.amirab.util.osfileutil.FileUtils
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import java.io.File

class AddDownloadChecker<
        Credentials : IDownloadCredentials,
        ResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        >(
    initialName: String,
    initialFolder: String,
    private val linkChecker: LinkChecker<Credentials, ResponseInfo, TDownloadSize>,
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

    val name = MutableStateFlow(initialName)
    val folder = MutableStateFlow(initialFolder)

    init {
        combine(
            this.name,
            this.folder,
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
        if (!linkChecker.isValidCredentials(linkChecker.credentials.value)) {
            return CanAddResult.InvalidUrl
        }
        if (!fileNameValid()) {
            return CanAddResult.InvalidFileName
        }
        val name = name.value
        val folder = folder.value
        val file = File(folder, name)
        val duplicateFilterByPath = DuplicateFilterByPath(file)
        val items = downloadSystem
            .getDownloadItemsBy(duplicateFilterByPath::isDuplicate)

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
