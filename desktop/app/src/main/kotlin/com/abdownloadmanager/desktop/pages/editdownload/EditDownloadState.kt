package com.abdownloadmanager.desktop.pages.editdownload

import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.desktop.utils.FileNameValidator
import com.abdownloadmanager.desktop.utils.LinkChecker
import com.abdownloadmanager.desktop.utils.convertSpeedToHumanReadable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.isValidUrl
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.downloaditem.DownloadItem.Companion.LENGTH_UNKNOWN
import ir.amirab.downloader.downloaditem.withCredentials
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.flow.onEachLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

sealed interface CanEditWarnings {
    fun asStringSource(): StringSource
    data class FileSizeNotMatch(
        val currentSize: Long,
        val newSize: Long,
    ) : CanEditWarnings {
        override fun asStringSource(): StringSource {
            return "The saved item have size of $currentSize and now will change to $newSize".asStringSource()
        }

    }
}

sealed interface CanEditDownloadResult {
    data object FileNameAlreadyExists : CanEditDownloadResult
    data object InvalidURL : CanEditDownloadResult
    data object InvalidFileName : CanEditDownloadResult
    data object NothingChanged : CanEditDownloadResult
    data object Waiting : CanEditDownloadResult
    data class CanEdit(
        val warnings: List<CanEditWarnings>,
    ) : CanEditDownloadResult
}

class EditDownloadChecker(
    val currentDownloadItem: MutableStateFlow<DownloadItem>,
    val editedDownloadItem: MutableStateFlow<DownloadItem>,
    val newLengthFlow: StateFlow<Long?>,
    val conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
) {
    init {
        editedDownloadItem
            .onEach {
                _canEditResult.value = CanEditDownloadResult.Waiting
            }.launchIn(scope)
    }

    fun check() {
        _canEditResult.value = CanEditDownloadResult.Waiting
        _canEditResult.value = check(
            current = currentDownloadItem.value,
            edited = editedDownloadItem.value,
            newLength = newLengthFlow.value,
        )
    }

    private fun check(
        current: DownloadItem,
        edited: DownloadItem,
        newLength: Long?,
    ): CanEditDownloadResult {
        if (current == edited) {
            return CanEditDownloadResult.NothingChanged
        }
        if (!isValidUrl(edited.link)) {
            return CanEditDownloadResult.InvalidURL
        }
        if (edited.name != current.name) {
            if (!FileNameValidator.isValidFileName(edited.name)) {
                return CanEditDownloadResult.InvalidFileName
            }
            if (conflictDetector.checkAlreadyExists(current, edited)) {
                return CanEditDownloadResult.FileNameAlreadyExists
            }
        }
        val warnings = mutableListOf<CanEditWarnings>()
        if (current.contentLength != newLength) {
            warnings.add(
                CanEditWarnings.FileSizeNotMatch(
                    currentSize = current.contentLength,
                    newSize = newLength ?: LENGTH_UNKNOWN,
                )
            )
        }
        return CanEditDownloadResult.CanEdit(warnings)
    }

    private val _canEditResult = MutableStateFlow<CanEditDownloadResult>(CanEditDownloadResult.NothingChanged)
    val canEditResult = _canEditResult.asStateFlow()
    val canEdit = canEditResult.mapStateFlow {
        it is CanEditDownloadResult.CanEdit
    }
}

interface DownloadConflictDetector {
    fun checkAlreadyExists(
        current: DownloadItem,
        edited: DownloadItem,
    ): Boolean
}

class EditDownloadState(
    val currentDownloadItem: MutableStateFlow<DownloadItem>,
    val editedDownloadItem: MutableStateFlow<DownloadItem>,
    val downloaderClient: DownloaderClient,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
) {
    private val _showMoreSettings = MutableStateFlow(false)
    val showMoreSettings = _showMoreSettings.asStateFlow()
    fun setShowMoreSettings(showMoreSettings: Boolean) {
        _showMoreSettings.value = showMoreSettings
    }

    val credentials = editedDownloadItem.mapTwoWayStateFlow(
        map = {
            DownloadCredentials.from(it)
        },
        unMap = {
            copy().withCredentials(it)
        }
    )
    val name = editedDownloadItem.mapTwoWayStateFlow(
        map = {
            it.name
        },
        unMap = {
            copy(name = it)
        }
    )
    val configurables = listOf(
        SpeedLimitConfigurable(
            Res.string.download_item_settings_speed_limit.asStringSource(),
            Res.string.download_item_settings_speed_limit_description.asStringSource(),
            backedBy = editedDownloadItem.mapTwoWayStateFlow(
                map = {
                    it.speedLimit
                },
                unMap = {
                    copy(speedLimit = it)
                }
            ),
            describe = {
                if (it == 0L) Res.string.unlimited.asStringSource()
                else convertSpeedToHumanReadable(it).asStringSource()
            }
        ),
        IntConfigurable(
            Res.string.settings_download_thread_count.asStringSource(),
            Res.string.settings_download_thread_count_description.asStringSource(),
            backedBy = editedDownloadItem.mapTwoWayStateFlow(
                map = {
                    it.preferredConnectionCount ?: 0
                },
                unMap = {
                    copy(
                        preferredConnectionCount = it.takeIf { it > 1 }
                    )
                }
            ),
            range = 0..32,
            describe = {
                if (it == 0) Res.string.use_global_settings.asStringSource()
                else Res.string.download_item_settings_thread_count_describe
                    .asStringSourceWithARgs(
                        Res.string.download_item_settings_thread_count_describe_createArgs(
                            count = it.toString()
                        )
                    )
            }
        ),
        StringConfigurable(
            Res.string.username.asStringSource(),
            Res.string.download_item_settings_username_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.username.orEmpty()
                },
                unMap = {
                    copy(username = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.password.asStringSource(),
            Res.string.download_item_settings_password_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.password.orEmpty()
                },
                unMap = {
                    copy(password = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.download_item_settings_download_page.asStringSource(),
            Res.string.download_item_settings_download_page_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.downloadPage.orEmpty()
                },
                unMap = {
                    copy(downloadPage = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
    )

    fun setName(name: String) {
        this.name.value = name
    }

    val link = credentials.mapTwoWayStateFlow(
        map = { it.link },
        unMap = {
            copy(link = it)
        }
    )

    fun setLink(link: String) {
        credentials.update {
            it.copy(link = link)
        }
    }

    fun importCredentials(importedCredentials: DownloadCredentials) {
        this.credentials.update {
            importedCredentials
        }
    }

    private val linkChecker = LinkChecker(
        initialCredentials = credentials.value,
        client = downloaderClient,
    )
    val isLinkLoading = linkChecker.isLoading

    val gettingResponseInfo = linkChecker.isLoading
    val responseInfo = linkChecker.responseInfo
    val length = linkChecker.length

    private val editDownloadChecker = EditDownloadChecker(
        currentDownloadItem = currentDownloadItem,
        editedDownloadItem = editedDownloadItem,
        newLengthFlow = length,
        conflictDetector = conflictDetector,
        scope = scope,
    )

    val canEditDownloadResult = editDownloadChecker.canEditResult
    val canEdit = editDownloadChecker.canEdit

    private val refreshResponseInfoImmediately = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    private val scheduleRefreshResponseInfo = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    private val scheduleRecheckEditDownloadIsPossible = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    fun refresh() {
        refreshResponseInfoImmediately.tryEmit(Unit)
    }

    private fun scheduleRefresh(
        alsoRecheckLink: Boolean,
    ) {
        if (alsoRecheckLink) {
            scheduleRefreshResponseInfo.tryEmit(Unit)
        }
        scheduleRecheckEditDownloadIsPossible.tryEmit(Unit)
    }

    init {
        merge(
            scheduleRefreshResponseInfo.debounce(500),
            refreshResponseInfoImmediately
        ).onEachLatest {
            linkChecker.check()
        }.launchIn(scope)
        merge(
            scheduleRecheckEditDownloadIsPossible.debounce(500),
//            ...
        ).onEachLatest {
            editDownloadChecker.check()
        }.launchIn(scope)

        credentials.onEach { credentials ->
            linkChecker.credentials.update { credentials }
            scheduleRefresh(alsoRecheckLink = true)
        }.launchIn(scope)
        editedDownloadItem.onEach {
            scheduleRefresh(alsoRecheckLink = false)
        }.launchIn(scope)
        length.onEach {
            scheduleRefresh(alsoRecheckLink = false)
        }.launchIn(scope)
    }
}