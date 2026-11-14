package com.abdownloadmanager.shared.pages.batchdownload

import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.extractors.linkextractor.StringUrlExtractor
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

open class BaseBatchDownloadComponent(
    ctx: ComponentContext,
    val onClose: () -> Unit,
    val importLinks: (List<String>) -> Unit,
) : BaseComponent(ctx), ContainsEffects<BaseBatchDownloadComponent.Effects> by supportEffects() {

    private val rangeRegex = Regex("""\[(\d+)-(\d+)\]""")

    private val _mode = MutableStateFlow(BatchDownloadMode.UrlList)
    val mode = _mode.asStateFlow()

    fun setMode(mode: BatchDownloadMode) {
        _mode.value = mode
    }

    private val _urlsText = MutableStateFlow("")
    val urlsText = _urlsText.asStateFlow()

    fun setUrlsText(text: String) {
        _urlsText.value = text
    }

    val parsedUrls: StateFlow<List<String>> = _urlsText.mapStateFlow { text ->
        StringUrlExtractor.extract(text)
    }

    private val _link = MutableStateFlow("")
    val link = _link.asStateFlow()

    fun setLink(link: String) {
        val trimmed = link.trim()
        val extracted = StringUrlExtractor.extract(trimmed)
        if (extracted.size > 1) {
            setMode(BatchDownloadMode.UrlList)
            setUrlsText(trimmed)
            return
        }
        val rangeMatch = rangeRegex.find(trimmed)
        if (rangeMatch != null) {
            val startVal = rangeMatch.groupValues[1]
            val endVal = rangeMatch.groupValues[2]
            val replaced = trimmed.replaceRange(rangeMatch.range, "*")
            _link.value = replaced
            if (_start.value.isBlank()) setStart(startVal)
            if (_end.value.isBlank()) setEnd(endVal)
            return
        }
        _link.value = link
    }

    private val _start = MutableStateFlow("")
    val start = _start.asStateFlow()

    fun setStart(start: String) {
        _start.value = start
    }

    private val _end = MutableStateFlow("")
    val end = _end.asStateFlow()

    fun setEnd(end: String) {
        _end.value = end
    }

    private val _wildcardLength = MutableStateFlow<WildcardLength>(WildcardLength.Auto)
    val wildcardLength = _wildcardLength
    fun setWildCardLength(wildcardLength: WildcardLength) {
        _wildcardLength.value = wildcardLength
    }

    init {
        fillLinkIfUrlIsInClipboard()
    }

    private fun fillLinkIfUrlIsInClipboard() {
        scope.launch {
            withContext(Dispatchers.Default) {
                val clipboard = ClipboardUtil.read() ?: return@withContext
                val trimmed = clipboard.trim()
                if (trimmed.isEmpty()) return@withContext

                val extracted = StringUrlExtractor.extract(trimmed)
                if (extracted.size > 1) {
                    setMode(BatchDownloadMode.UrlList)
                    setUrlsText(trimmed)
                } else if (extracted.size == 1) {
                    val singleUrl = extracted.first()
                    val rangeMatch = rangeRegex.find(singleUrl)
                    if (singleUrl.contains("*") || rangeMatch != null) {
                        setMode(BatchDownloadMode.Wildcard)
                        setLink(singleUrl)
                    } else {
                        setMode(BatchDownloadMode.UrlList)
                        setUrlsText(singleUrl)
                        setLink(singleUrl)
                    }
                } else if (trimmed.contains("*")) {
                    setMode(BatchDownloadMode.Wildcard)
                    setLink(trimmed)
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private val batch = combineStateFlows(
        link,
        start,
        end,
        wildcardLength,
    ) { link, start, end, wildcardLength ->
        val minimumSize = max(start.length, end.length)
        val start = start.toIntOrNull() ?: return@combineStateFlows null
        val end = end.toIntOrNull() ?: return@combineStateFlows null
        if (start < 0) return@combineStateFlows null
        if (end < 0 || end < start) return@combineStateFlows null
        WildcardString(
            string = link.trim(),
            range = start..end,
            wildcardLength = wildcardLength,
            minimumAllowed = minimumSize,
        )
    }

    val startLinkResult: StateFlow<String> = batch
        .mapStateFlow { it?.first() ?: "" }
    val endLinkResult: StateFlow<String> = batch
        .mapStateFlow { it?.last() ?: "" }

    val validationResult: StateFlow<BatchDownloadValidationResult> = combineStateFlows(
        mode,
        batch,
        parsedUrls,
        urlsText,
    ) { mode, batch, parsedUrls, urlsText ->
        when (mode) {
            BatchDownloadMode.Wildcard -> {
                when (batch) {
                    null -> BatchDownloadValidationResult.Others
                    else -> {
                        val listSize = batch.size()
                        when {
                            listSize < 1 -> BatchDownloadValidationResult.Others
                            listSize > MAX_ALLOWED_RANGE -> BatchDownloadValidationResult.MaxRangeExceed(MAX_ALLOWED_RANGE)
                            !HttpUrlUtils.isValidUrl(batch.first()) -> BatchDownloadValidationResult.URLInvalid
                            else -> BatchDownloadValidationResult.Ok
                        }
                    }
                }
            }
            BatchDownloadMode.UrlList -> {
                when {
                    urlsText.isBlank() -> BatchDownloadValidationResult.Others
                    parsedUrls.isEmpty() -> BatchDownloadValidationResult.NoUrlsFound
                    parsedUrls.size > MAX_ALLOWED_RANGE -> BatchDownloadValidationResult.MaxRangeExceed(MAX_ALLOWED_RANGE)
                    else -> BatchDownloadValidationResult.Ok
                }
            }
        }
    }

    val canConfirm = validationResult.mapStateFlow {
        it is BatchDownloadValidationResult.Ok
    }

    fun confirm() {
        if (!canConfirm.value) {
            return
        }
        val items = when (mode.value) {
            BatchDownloadMode.Wildcard -> batch.value?.toList()
            BatchDownloadMode.UrlList -> parsedUrls.value
        }?.takeIf { it.isNotEmpty() }

        if (items != null) {
            importLinks(items)
        }
        onClose()
    }

    companion object {
        const val MAX_ALLOWED_RANGE = 1000
    }

    sealed interface Effects {
        interface PlatformEffects : Effects
    }
}

enum class BatchDownloadMode {
    UrlList,
    Wildcard,
}

sealed interface BatchDownloadValidationResult {
    data object Ok : BatchDownloadValidationResult
    data object Others : BatchDownloadValidationResult
    data class MaxRangeExceed(val allowed: Int) : BatchDownloadValidationResult
    data object URLInvalid : BatchDownloadValidationResult
    data object NoUrlsFound : BatchDownloadValidationResult
}

sealed class WildcardLength {
    data object Auto : WildcardLength()
    data object Unspecified : WildcardLength()
    data class Custom(val v: Int) : WildcardLength()
}

data class WildcardString(
    val string: String,
    val range: IntRange,
    val wildcardLength: WildcardLength,
    val minimumAllowed: Int = range.last.toString().length,
) : Iterable<String> {
    private fun transformIndex(index: Int): String {
        var str = index.toString()
        if (wildcardLength is WildcardLength.Unspecified) {
            return str
        }
        val length = when (wildcardLength) {
            is WildcardLength.Custom -> wildcardLength.v.coerceAtLeast(minimumAllowed)
            WildcardLength.Auto -> minimumAllowed
            WildcardLength.Unspecified -> null
        }
        if (length != null) {
            str = str.padStart(length, '0')
        }
        return str
    }

    fun get(index: Int): String {
        return string.replace("*", transformIndex(index))
    }

    fun first(): String {
        return get(range.first)
    }

    fun last(): String {
        return get(range.last)
    }

    fun size() = range.last - range.first + 1

    override fun iterator(): Iterator<String> {
        return range
            .asSequence()
            .map(::get)
            .iterator()
    }
}
