package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.downloader.exception.FileChangedException
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs

object DefaultDownloadErrorMapper : DownloadErrorMapper {

    override fun accept(throwable: Throwable): Boolean {
        return true
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        val exceptionName = throwable::class.simpleName
            ?: Res.string.unknown.asStringSource().getString()
        val message = throwable.localizedMessage
            ?: Res.string.unknown.asStringSource().getString()
        return createErrorReason(
            title = Res.string.download_error_reason_default_title.asStringSource().getString(),
            description = Res.string.download_error_reason_default_description.asStringSourceWithARgs(
                Res.string.download_error_reason_default_description_createArgs(
                    exceptionName = exceptionName,
                    exceptionMessage = message,
                )
            ).getString(),
            suggestion = Res.string.download_error_reason_default_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
