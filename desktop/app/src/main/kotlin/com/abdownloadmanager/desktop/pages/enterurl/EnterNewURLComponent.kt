package com.abdownloadmanager.desktop.pages.enterurl

import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.util.extractors.linkextractor.StringUrlExtractor
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DownloaderSelection {
    data object Auto : DownloaderSelection
    data class Fixed(
        val downloaderInUi: TADownloaderInUI,
    ) : DownloaderSelection
}

class EnterNewURLComponent(
    val ctx: ComponentContext,
    val config: Config,
    val downloaderInUiRegistry: DownloaderInUiRegistry,
    private val onCloseRequest: () -> Unit,
    private val onRequestFinished: (IDownloadCredentials) -> Unit,
) : BaseComponent(
    ctx
), ContainsEffects<EnterNewURLComponent.Effects> by supportEffects() {
    private val _url: MutableStateFlow<String> = MutableStateFlow("")
    val url = _url.asStateFlow()

    val bestDownloader = url.mapStateFlow {
        downloaderInUiRegistry.bestMatchForThisLink(it)
    }
    private val _downloaderSelection = MutableStateFlow<DownloaderSelection>(
        DownloaderSelection.Auto
    )
    val downloaderSelection = _downloaderSelection.asStateFlow()
    fun selectDownloader(downloaderSelection: DownloaderSelection) {
        _downloaderSelection.value = downloaderSelection
    }


    val downloaderToPickup: StateFlow<TADownloaderInUI?> = combineStateFlows(
        bestDownloader,
        downloaderSelection,
    ) { bestDownloader, userSelection ->
        when (userSelection) {
            DownloaderSelection.Auto -> bestDownloader
            is DownloaderSelection.Fixed -> userSelection.downloaderInUi
        }
    }
    val canAdd = combineStateFlows(
        downloaderToPickup,
        url,
    ) { downloader, url ->
        url.isNotBlank() && downloader != null
    }


    fun setURL(url: String) {
        _url.value = url
    }

    fun bringToFront() {
        sendEffect(Effects.BringToFront)
    }

    var firstTimeOpened = false
    fun onPageOpen() {
        if (!firstTimeOpened) {
            scope.launch {
                if (fillLinkIfThereIsALinkInClipboard()) {
                    sendEffect(Effects.LinkSelectAll)
                }
            }
            firstTimeOpened = true
        }
    }

    private fun fillLinkIfThereIsALinkInClipboard(): Boolean {
        val possibleLinks = ClipboardUtil.read() ?: return false
        val downloadLinks = StringUrlExtractor.extract(possibleLinks)
        if (downloadLinks.size == 1) {
            setURL(downloadLinks.first())
            return true
        }
        return false
    }


    fun close() {
        scope.launch {
            onCloseRequest()
        }
    }

    fun newDownloadEntered() {
        val downloader = downloaderToPickup.value ?: return
        val link = url.value
        onRequestFinished(
            downloader.createMinimumCredentials(link)
        )
    }

    val possibleValues = buildList {
        add(DownloaderSelection.Auto)
        addAll(downloaderInUiRegistry.getAll().map {
            DownloaderSelection.Fixed(it)
        })
    }

    data object Config

    sealed interface Effects {
        data object BringToFront : Effects
        data object LinkSelectAll : Effects
    }
}
