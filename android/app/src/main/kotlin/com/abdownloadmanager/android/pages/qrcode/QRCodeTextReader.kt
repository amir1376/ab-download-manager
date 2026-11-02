package com.abdownloadmanager.android.pages.qrcode

import com.abdownloadmanager.shared.util.qrcode.MultiQRCode
import com.abdownloadmanager.shared.util.qrcode.MultiQRCodeInfo
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class QRCodeTextReader {
    var currentId: String = ""
    val readChunks = mutableMapOf<Int, String>()
    val readChunksStats = MutableStateFlow(
        mapOf(0 to false)
    )

    val totalCount: StateFlow<Int> = readChunksStats.mapStateFlow {
        it.size
    }
    val readCount: StateFlow<Int> = readChunksStats.mapStateFlow {
        it.values.count { it }
    }

    val resultReady: SharedFlow<String>
        field = MutableSharedFlow(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    @Synchronized
    fun addSegments(text: String) {
        val result = MultiQRCode.getMultiQRCodeInfo(text)
        if (result == null) {
            singleQR(text)
            return
        }
        tryScanMultiQR(result)
    }

    private fun singleQR(text: String) {
        currentId = ""
        readChunks.clear()
        readChunksStats.value = mapOf(
            0 to true
        )
        resultReady.tryEmit(text)
    }

    private fun tryScanMultiQR(result: MultiQRCodeInfo) {
        try {
            validateResult(result)
        } catch (_: Exception) {
            return
        }
        if (result.id != currentId) {
            startReadNewMultiQR(result)
        }
        continueReadMultiQR(result)
        checkIfFinished()
    }

    private fun validateResult(result: MultiQRCodeInfo) {
        require(result.totalItems > result.index)
    }

    private fun checkIfFinished() {
        if (readChunks.size != totalCount.value) {
            return
        }
        val chunkList = readChunks.entries
            .sortedBy { it.key }
            .map { it.value }
        val totalSize = chunkList.sumOf { it.length }
        val data = buildString(totalSize) {
            chunkList.forEach { chunk ->
                append(chunk)
            }
        }
        resultReady.tryEmit(data)
    }

    private fun continueReadMultiQR(result: MultiQRCodeInfo) {
        if (!readChunks.containsKey(result.index)) {
            readChunks[result.index] = result.data
            readChunksStats.update {
                it.toMutableMap().also {
                    it[result.index] = true
                }.toMap()
            }
        }
    }

    private fun startReadNewMultiQR(result: MultiQRCodeInfo) {
        currentId = result.id
        readChunksStats.value = buildMap {
            repeat(result.totalItems) {
                this[it] = false
            }
        }
        readChunks.clear()
    }

}
