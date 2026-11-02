package com.abdownloadmanager.shared.util.qrcode

object MultiQRCode {
    private val REGEX = "^<qr_(?<id>[a-z0-9]+)/(?<index>\\d+)/(?<total>\\d+)>(?<data>[\\s\\S]+)$".toRegex()

    private fun createChunk(
        id: String,
        index: Int,
        totalItems: Int,
        content: String,
    ): String {
        return "<qr_$id/$index/$totalItems>$content"
    }

    fun getMultiQRCodeInfo(qrText: String): MultiQRCodeInfo? {
        return runCatching {
            REGEX.matchEntire(qrText)?.let {
                MultiQRCodeInfo(
                    id = it.groups.get("id")!!.value,
                    index = it.groups.get("index")!!.value.toInt(),
                    totalItems = it.groups.get("total")!!.value.toInt(),
                    data = it.groups.get("data")!!.value,
                )
            }
        }.getOrNull()
    }

    private fun createId(string: String): String {
        val crc = java.util.zip.CRC32()
        crc.update(string.toByteArray())
        return crc.value.toString(36).takeLast(4)
    }

    fun createMultiQRCode(
        content: String,
        textLength: Int,
    ): List<String> {
        val chunked = content.chunked(textLength)
        val id = createId(content)
        return chunked
            .mapIndexed { index, string ->
                createChunk(
                    id = id,
                    index = index,
                    totalItems = chunked.size,
                    content = string
                )
            }
    }
}

data class MultiQRCodeInfo(
    val id: String,
    val totalItems: Int,
    val index: Int,
    val data: String,
)
