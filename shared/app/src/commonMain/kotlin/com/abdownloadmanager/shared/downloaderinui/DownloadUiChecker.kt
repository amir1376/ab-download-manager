package com.abdownloadmanager.shared.downloaderinui

import com.abdownloadmanager.shared.downloaderinui.add.AddDownloadChecker
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.flow.onEachLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

abstract class DownloadUiChecker<
        TCredentials : IDownloadCredentials,
        TResponseInfoType : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfoType>,
        >(
    initialCredentials: TCredentials,
    linkCheckerFactory: LinkCheckerFactory<TCredentials, TResponseInfoType, TLinkChecker>,
    initialFolder: String,
    initialName: String = "",
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) {
    val credentials = MutableStateFlow(initialCredentials)
    val name = MutableStateFlow(initialName)
    val folder = MutableStateFlow(initialFolder)


    protected val linkChecker = linkCheckerFactory.createLinkChecker(credentials.value)
    protected val addDownloadChecker = AddDownloadChecker(
        linkChecker = linkChecker,
        initialName = name.value,
        initialFolder = folder.value,
        downloadSystem = downloadSystem,
        parentScope = scope,
    )
    val gettingResponseInfo = linkChecker.isLoading
    val responseInfo = linkChecker.responseInfo

    val canAddToDownloadResult = addDownloadChecker.canAddResult
    val canAdd = addDownloadChecker.canAdd
    val isDuplicate = addDownloadChecker.isDuplicate

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
            addDownloadChecker.check()
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
            if (addDownloadChecker.name.value != name) {
                addDownloadChecker.name.update { name }
                scheduleRefresh(alsoRecheckLink = false)
            }
        }.launchIn(scope)
        folder.onEach { folder ->
            if (addDownloadChecker.folder.value != folder) {
                addDownloadChecker.folder.update { folder }
                scheduleRefresh(alsoRecheckLink = false)
            }
        }.launchIn(scope)
    }
}
