package com.abdownloadmanager.desktop.pages.multipledownloads

import com.abdownloadmanager.shared.util.extractors.linkextractor.StringUrlExtractor
import ir.amirab.util.HttpUrlUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Resolves one link at a time; actual transfers use the application's normal download queue. */
class MultipleDownloadsController<T>(
    private val scope: CoroutineScope,
    initialFolder: String,
    private val prepare: suspend (String, String) -> T,
    private val enqueue: suspend (T) -> String,
    private val activeDownloads: () -> Map<String, String>,
) {
    enum class Status { Pending, Resolving, Queued, Ready, Failed }
    data class Item(val url: String, val status: Status = Status.Pending, val detail: String = "")

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()
    private val _folder = MutableStateFlow(initialFolder)
    val folder = _folder.asStateFlow()
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()
    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()
    private var job: Job? = null

    fun setText(value: String) {
        if (_running.value) return
        val previous = _items.value.associateBy { linkKey(it.url) }
        _text.value = value
        _items.value = value.lineSequence().flatMap { StringUrlExtractor.extract(it).asSequence() }
            .distinctBy(::linkKey)
            .map { previous[linkKey(it)]?.copy(url = it) ?: Item(it) }
            .toList()
        refreshActiveDownloads()
    }

    fun setFolder(value: String) {
        if (!_running.value) _folder.value = value
    }

    fun start() {
        if (_running.value || _folder.value.isBlank()) return
        refreshActiveDownloads()
        val pending = _items.value.filter { it.status != Status.Queued }
        if (pending.isEmpty()) return
        val destination = _folder.value.trim()
        _running.value = true
        job = scope.launch {
            try {
                for (item in pending) {
                    currentCoroutineContext().ensureActive()
                    val activeName = activeDownloadNames()[linkKey(item.url)]
                    if (activeName != null) {
                        update(item.url, Status.Queued, activeName)
                        continue
                    }
                    update(item.url, Status.Resolving)
                    try {
                        val prepared = prepare(item.url, destination)
                        currentCoroutineContext().ensureActive()
                        // Finish recording an accepted item even if Stop is pressed during the local enqueue.
                        withContext(NonCancellable) {
                            // Another app window may have started this link during metadata lookup.
                            val name = activeDownloadNames()[linkKey(item.url)] ?: enqueue(prepared)
                            update(item.url, Status.Queued, name)
                        }
                    } catch (error: CancellationException) {
                        if (_items.value.any { it.url == item.url && it.status == Status.Resolving }) {
                            update(item.url, Status.Pending)
                        }
                        throw error
                    } catch (error: Exception) {
                        update(item.url, Status.Failed, error.message ?: error.javaClass.simpleName)
                    }
                }
            } finally {
                _running.value = false
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    /** The queue is authoritative; an earlier import never permanently blocks a link. */
    fun refreshActiveDownloads() {
        val active = activeDownloadNames()
        _items.update { items ->
            items.map { item ->
                val name = active[linkKey(item.url)]
                when {
                    item.status == Status.Resolving -> item
                    name != null -> item.copy(status = Status.Queued, detail = name)
                    item.status == Status.Queued -> item.copy(status = Status.Ready)
                    else -> item
                }
            }
        }
    }

    private fun activeDownloadNames() = activeDownloads().mapKeys { linkKey(it.key) }

    private fun update(url: String, status: Status, detail: String = "") {
        _items.update { items -> items.map { if (it.url == url) it.copy(status = status, detail = detail) else it } }
    }

    companion object {
        fun linkKey(url: String): String = runCatching {
            HttpUrlUtils.createURL(url).newBuilder().fragment(null).build().toString()
        }.getOrElse { url.substringBefore('#') }
    }
}
