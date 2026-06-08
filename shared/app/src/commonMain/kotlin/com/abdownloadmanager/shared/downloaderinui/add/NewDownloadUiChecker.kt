package com.abdownloadmanager.shared.downloaderinui.add

import arrow.core.identity
import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.flow.onEachLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

abstract class NewDownloadUiChecker<
        TCredentials : IDownloadCredentials,
        TResponseInfoType : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfoType, TDownloadSize>,
        >(
    initialCredentials: TCredentials,
    linkCheckerFactory: LinkCheckerFactory<TCredentials, TResponseInfoType, TDownloadSize, TLinkChecker>,
    initialFolder: String,
    initialName: String = "",
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) {
    val credentials = MutableStateFlow(initialCredentials)
    val name = MutableStateFlow(initialName)
    val folder = MutableStateFlow(initialFolder)

    protected val linkChecker = linkCheckerFactory.createLinkChecker(credentials.value)
    protected val newDownloadChecker = NewDownloadChecker(
        linkChecker = linkChecker,
        initialName = name.value,
        initialFolder = folder.value,
        downloadSystem = downloadSystem,
        parentScope = scope,
    )
    val downloadSize: StateFlow<TDownloadSize?> = linkChecker.downloadSize

    val gettingResponseInfo = linkChecker.isLoading
    val responseResult = linkChecker.responseResult
    val responseInfo = linkChecker.responseInfo
    val lastErrorReason = responseResult.map { result ->
        result
            ?.fold(
                onSuccess = { it.unsuccessFullException },
                onFailure = ::identity
            )
            ?.let(downloadSystem.errorMapperRegistry::getReason)

    }.stateIn(scope, started = SharingStarted.Eagerly, null)

    val canAddToDownloadResult = newDownloadChecker.canAddResult
    val canAdd = newDownloadChecker.canAdd
    val isDuplicate = newDownloadChecker.isDuplicate

    private val refreshResponseInfoImmediately = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    private val scheduleRefreshResponseInfo = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    private val scheduleRecheckAddToDownloadIsPossible = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    fun refresh() {
        refreshResponseInfoImmediately.tryEmit(Unit)
    }

    private fun scheduleRefresh(
        alsoRecheckLink: Boolean
    ) {
        if (alsoRecheckLink) {
            scheduleRefreshResponseInfo.tryEmit(Unit)
        }
        scheduleRecheckAddToDownloadIsPossible.tryEmit(Unit)
    }

    init {
        merge(
            scheduleRefreshResponseInfo.debounce(500),
            refreshResponseInfoImmediately
        ).onEachLatest {
            linkChecker.check()
        }.launchIn(scope)
        merge(
            scheduleRecheckAddToDownloadIsPossible,
//            ...
        ).onEachLatest {
            newDownloadChecker.check()
        }.launchIn(scope)



        linkChecker.suggestedName
            .onEach {
                it?.let { name ->
                    this.name.update { name }
                }
            }.launchIn(scope)


        credentials.onEach { credentials ->
            linkChecker.credentials.update { credentials }
            scheduleRefresh(alsoRecheckLink = true)
        }.launchIn(scope)
        name.onEach { name ->
            if (newDownloadChecker.name.value != name) {
                newDownloadChecker.name.update { name }
                scheduleRefresh(alsoRecheckLink = false)
            }
        }.launchIn(scope)
        folder.onEach { folder ->
            if (newDownloadChecker.folder.value != folder) {
                newDownloadChecker.folder.update { folder }
                scheduleRefresh(alsoRecheckLink = false)
            }
        }.launchIn(scope)
    }
}
