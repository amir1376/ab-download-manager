package com.abdownloadmanager.desktop.pages.batchdownload

import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.shared.utils.isValidUrl
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

sealed interface BatchDownloadEffects {
    data object BringToFront : BatchDownloadEffects
}
class BatchDownloadComponent(
    ctx: ComponentContext,
    val onClose: () -> Unit,
    val importLinks: (List<String>) -> Unit,
) : BaseComponent(ctx), ContainsEffects<BatchDownloadEffects> by supportEffects() {

    private val _link = MutableStateFlow("")
    val link = _link.asStateFlow()

    fun setLink(link: String) {
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
                if (isValidUrl(clipboard)) {
                    setLink(clipboard.trim())
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


    fun bringToFront() {
        sendEffect(BatchDownloadEffects.BringToFront)
    }


    val startLinkResult: StateFlow<String> = batch
        .mapStateFlow { it?.first() ?: "" }
    val endLinkResult: StateFlow<String> = batch
        .mapStateFlow { it?.last() ?: "" }


    val validationResult = batch.mapStateFlow {
        when (it) {
            null -> BatchDownloadValidationResult.Others
            else -> {
                val listSize = it.size()
                when {
                    listSize < 1 -> BatchDownloadValidationResult.Others
                    listSize > MAX_ALLOWED_RANGE -> BatchDownloadValidationResult.MaxRangeExceed(MAX_ALLOWED_RANGE)
                    !isValidUrl(it.first()) -> BatchDownloadValidationResult.URLInvalid
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
            println(batch.value?.toList())
            return
        }
        val items = batch.value?.toList()?.takeIf { it.isNotEmpty() }
        if (items != null) {
            importLinks(items)
        }
        onClose()
    }

    companion object {
        const val MAX_ALLOWED_RANGE = 1000
    }
}

sealed interface BatchDownloadValidationResult {
    data object Ok : BatchDownloadValidationResult
    data object Others : BatchDownloadValidationResult
    data class MaxRangeExceed(val allowed: Int) : BatchDownloadValidationResult
    data object URLInvalid : BatchDownloadValidationResult
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