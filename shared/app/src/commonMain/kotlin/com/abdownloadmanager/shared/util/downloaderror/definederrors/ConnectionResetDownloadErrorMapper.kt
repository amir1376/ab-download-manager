package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import java.net.SocketException

object ConnectionResetDownloadErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return (throwable is SocketException && throwable.message?.contains("Connection reset") == true)
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        return createErrorReason(
            title = Res.string.download_error_reason_connection_reset_title.asStringSource().getString(),
            description = Res.string.download_error_reason_connection_reset_description.asStringSource().getString(),
            suggestion = Res.string.download_error_reason_connection_reset_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
