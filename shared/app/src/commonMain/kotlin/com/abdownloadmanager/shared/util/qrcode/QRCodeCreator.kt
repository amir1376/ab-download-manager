package com.abdownloadmanager.shared.util.qrcode

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.resources.Strings.items

@Immutable
sealed interface CreatedQRCode {
    @Immutable
    data class Single(val item: String) : CreatedQRCode

    @Immutable
    data class Multiple(val items: List<String>) : CreatedQRCode

    fun firstItem(): String {
        return when (this) {
            is Multiple -> items.first()
            is Single -> item
        }
    }
    fun asList(): List<String> {
        return when (this) {
            is Single -> listOf(item)
            is Multiple -> items
        }
    }

    fun getFull(): String {
        return asList().joinToString("")
    }
}

object QRCodeCreator {
    const val MULTI_QR_DELAY_MILLIS = 500L
    const val SINGLE_QR_DEFAULT_SIZE = 500
    const val MULTI_QR_DEFAULT_CHUNK_SIZE = 400
    fun createQRCode(
        content: String,
        maxSingleQRCodeLength: Int = SINGLE_QR_DEFAULT_SIZE,
        maxMultiQRCodeLength: Int = MULTI_QR_DEFAULT_CHUNK_SIZE,
    ): CreatedQRCode {
        if (content.length <= maxSingleQRCodeLength) {
            return CreatedQRCode.Single(content)
        }
        return CreatedQRCode.Multiple(
            MultiQRCode.createMultiQRCode(
                content = content,
                textLength = maxMultiQRCodeLength,
            )
        )
    }
}
