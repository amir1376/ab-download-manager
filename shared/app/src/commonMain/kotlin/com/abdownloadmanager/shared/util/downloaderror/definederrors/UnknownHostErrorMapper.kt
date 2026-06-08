package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.util.compose.asStringSource
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object UnknownHostErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return throwable is UnknownHostException
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        return createErrorReason(
            title = Res.string.download_error_reason_unknown_host_title.asStringSource().getString(),
            description = Res.string.download_error_reason_unknown_host_description.asStringSource().getString(),
            suggestion = Res.string.download_error_reason_unknown_host_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
