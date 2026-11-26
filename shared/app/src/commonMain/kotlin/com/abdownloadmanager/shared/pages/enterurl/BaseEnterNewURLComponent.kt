package com.abdownloadmanager.shared.pages.enterurl

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.extractors.linkextractor.DownloadCredentialsFromSimpleLink
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.extractors.linkextractor.StringUrlExtractor
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.abdownloadmanager.shared.util.realdebrid.RealDebridException
import com.abdownloadmanager.shared.util.realdebrid.RealDebridResolver
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

open class BaseEnterNewURLComponent(
    val ctx: ComponentContext,
    val config: Config,
    val downloaderInUiRegistry: DownloaderInUiRegistry,
    private val onCloseRequest: () -> Unit,
    private val onRequestFinished: (IDownloadCredentials) -> Unit,
    private val onRequestFinishedMultiple: ((List<IDownloadCredentials>) -> Unit)? = null,
) : BaseComponent(ctx),
    ContainsEffects<BaseEnterNewURLComponent.Effects> by supportEffects(),
    KoinComponent {
    // Real-Debrid is optional: it is only registered by platform DI modules that support it
    private val realDebridResolver: RealDebridResolver? = runCatching {
        getKoin().get<RealDebridResolver>()
    }.getOrNull()

    private val _url: MutableStateFlow<String> = MutableStateFlow("")
    val url = _url.asStateFlow()

    val bestDownloader = url.mapStateFlow {
        downloaderInUiRegistry.bestMatchForThisLink(it)
    }

    private val realDebridHandlesLink = url.mapStateFlow {
        realDebridResolver?.supportsThisLink(it) == true
    }

    private val _isResolvingRealDebrid = MutableStateFlow(false)
    val isResolvingRealDebrid = _isResolvingRealDebrid.asStateFlow()

    private val _realDebridError = MutableStateFlow<String?>(null)
    val realDebridError = _realDebridError.asStateFlow()
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
        realDebridHandlesLink,
        _isResolvingRealDebrid,
    ) { downloader, url, realDebridHandlesLink, isResolvingRealDebrid ->
        url.isNotBlank() && !isResolvingRealDebrid && (downloader != null || realDebridHandlesLink)
    }


    fun setURL(url: String) {
        _url.value = url
        _realDebridError.value = null
    }

    var firstTimeOpened = false
    open val shouldFillWithClipboard = true
    fun onPageOpen() {
        if (!firstTimeOpened) {
            if (shouldFillWithClipboard) {
                scope.launch {
                    if (fillLinkIfThereIsALinkInClipboard()) {
                        sendEffect(Effects.LinkSelectAll)
                    }
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
        val link = url.value.trim()
        val resolver = realDebridResolver
        if (resolver != null && resolver.supportsThisLink(link)) {
            resolveWithRealDebrid(resolver, link)
            return
        }
        val downloader = downloaderToPickup.value ?: return
        onRequestFinished(
            downloader.createMinimumCredentials(link)
        )
        onCloseRequest()
    }

    private fun resolveWithRealDebrid(resolver: RealDebridResolver, link: String) {
        if (_isResolvingRealDebrid.value) return
        scope.launch {
            _isResolvingRealDebrid.value = true
            _realDebridError.value = null
            try {
                val resolvedLinks = resolver.resolve(link)
                val credentials = resolvedLinks.mapNotNull { resolvedLink ->
                    downloaderInUiRegistry
                        .bestMatchForThisLink(resolvedLink.directLink)
                        ?.createMinimumCredentials(resolvedLink.directLink)
                }
                when {
                    credentials.isEmpty() -> {
                        _realDebridError.value = "Real-Debrid did not return any downloadable file"
                    }

                    credentials.size == 1 -> {
                        onRequestFinished(credentials.first())
                        onCloseRequest()
                    }

                    else -> {
                        val multipleHandler = onRequestFinishedMultiple
                        if (multipleHandler != null) {
                            multipleHandler(credentials)
                        } else {
                            onRequestFinished(credentials.first())
                        }
                        onCloseRequest()
                    }
                }
            } catch (e: RealDebridException) {
                _realDebridError.value = e.message
            } catch (e: Throwable) {
                _realDebridError.value = e.message ?: "Real-Debrid request failed"
            } finally {
                _isResolvingRealDebrid.value = false
            }
        }
    }

    val possibleValues = buildList {
        add(DownloaderSelection.Auto)
        addAll(downloaderInUiRegistry.getAll().map {
            DownloaderSelection.Fixed(it)
        })
    }

    interface Config

    sealed interface Effects {
        data object LinkSelectAll : Effects
        interface PlatformEffects : Effects
    }
}
