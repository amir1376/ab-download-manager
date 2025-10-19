package com.abdownloadmanager.desktop.pages.addDownload

import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.shared.utils.FilenameFixer
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

abstract class AddDownloadComponent(
    ctx: ComponentContext,
    val id: String,
) : BaseComponent(ctx), KoinComponent {
    companion object {
        const val lastLocationsCacheSize = 4

    }

    private var dialogUsed = false
    protected fun consumeDialog(block: () -> Unit) {
        if (dialogUsed) {
            return
        }
        block()
        dialogUsed = true
    }

    val pageStatesStorage: PageStatesStorage by inject()
    private val _lastUsedLocations = pageStatesStorage.lastUsedSaveLocations
    val lastUsedLocations: StateFlow<List<String>> = _lastUsedLocations.asStateFlow()
    fun addToLastUsedLocations(saveLocation: String) {
        _lastUsedLocations.update {
            buildList {
                add(saveLocation)
                addAll(it)
            }
                .distinct()
                .take(lastLocationsCacheSize)
        }
    }

    fun removeFromLastDownloadLocation(saveLocation: String) {
        _lastUsedLocations.update {
            it.filter { it != saveLocation }
        }
    }

    abstract val shouldShowWindow: StateFlow<Boolean>
}

interface AddDownloadConfig {
    val id: String
    val importOptions: ImportOptions

    data class SingleAddConfig(
        val credentials: AddDownloadCredentialsInUiProps,
        override val importOptions: ImportOptions = ImportOptions(),
        override val id: String = UUID.randomUUID().toString(),
    ) : AddDownloadConfig

    data class MultipleAddConfig(
        val links: List<AddDownloadCredentialsInUiProps> = emptyList(),
        override val importOptions: ImportOptions = ImportOptions(),
        override val id: String = UUID.randomUUID().toString(),
    ) : AddDownloadConfig

}

data class AddDownloadCredentialsInUiProps(
    val credentials: IDownloadCredentials,
    val extraConfig: Configs = Configs(),
) {
    data class Configs(
        // don't consume it directly as it might not be a valid file name on user's current OS
        val suggestedName: String? = null,
    ) {
        fun getAndFixSuggestedName(): String? {
            return suggestedName?.let(FilenameFixer::fix)
        }
    }
}
