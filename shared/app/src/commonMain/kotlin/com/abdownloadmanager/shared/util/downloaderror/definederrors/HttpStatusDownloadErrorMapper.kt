package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.downloader.exception.UnSuccessfulResponseException
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs

object HttpStatusDownloadErrorMapper : DownloadErrorMapper {
    private data class TitleDescriptionSuggestion(
        val title: String,
        val description: String,
        val suggestion: String
    )

    override fun accept(throwable: Throwable): Boolean {
        return throwable is UnSuccessfulResponseException
    }

    private fun getErrorFromCode(code: Int, message: String): TitleDescriptionSuggestion {
        val title: StringSource
        val description: StringSource
        val suggestion: StringSource
        when (code) {
            401 -> {
                title = Res.string.download_error_reason_http_401_title.asStringSource()
                description = Res.string.download_error_reason_http_401_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_401_suggestion.asStringSource()
            }

            403 -> {
                title = Res.string.download_error_reason_http_403_title.asStringSource()
                description = Res.string.download_error_reason_http_403_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_403_suggestion.asStringSource()
            }

            404 -> {
                title = Res.string.download_error_reason_http_404_title.asStringSource()
                description = Res.string.download_error_reason_http_404_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_404_suggestion.asStringSource()
            }

            407 -> {
                title = Res.string.download_error_reason_http_407_title.asStringSource()
                description = Res.string.download_error_reason_http_407_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_407_suggestion.asStringSource()
            }

            429 -> {
                title = Res.string.download_error_reason_http_429_title.asStringSource()
                description = Res.string.download_error_reason_http_429_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_429_suggestion.asStringSource()
            }

            in 400..499 -> {
                title = Res.string.download_error_reason_http_4xx_title.asStringSource()
                description = Res.string.download_error_reason_http_4xx_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_4xx_suggestion.asStringSource()
            }

            503 -> {
                title = Res.string.download_error_reason_http_503_title.asStringSource()
                description = Res.string.download_error_reason_http_503_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_503_suggestion.asStringSource()
            }

            in 500..599 -> {
                title = Res.string.download_error_reason_http_5xx_title.asStringSource()
                description = Res.string.download_error_reason_http_5xx_description.asStringSource()
                suggestion = Res.string.download_error_reason_http_5xx_suggestion.asStringSource()
            }

            else -> {
                title = Res.string.download_error_reason_http_default_title.asStringSource()
                description = Res.string.download_error_reason_http_default_description.asStringSourceWithARgs(
                    Res.string.download_error_reason_http_default_description_createArgs(
                        statusCode = code.toString(),
                        statusMessage = message,
                    )
                )
                suggestion = Res.string.download_error_reason_http_default_suggestion.asStringSource()
            }
        }
        return TitleDescriptionSuggestion(
            title.getString(), description.getString(), suggestion.getString()
        )
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        throwable as UnSuccessfulResponseException
        val code = throwable.code
        val msg = throwable.msg
        val reason = getErrorFromCode(code, msg)
        return createErrorReason(
            title = reason.title,
            description = reason.description,
            suggestion = reason.suggestion,
            throwable = throwable,
        )
    }
}
