package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.downloader.exception.ServerPartIsNotTheSameAsWeExpectException
import ir.amirab.downloader.exception.ServerResumeSupportChangeException
import ir.amirab.util.compose.asStringSource

object ResumeSupportChangedDownloadErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return throwable is ServerResumeSupportChangeException
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        return createErrorReason(
            title = Res.string.download_error_reason_server_resume_change_title.asStringSource().getString(),
            description = Res.string.download_error_reason_server_resume_change_description.asStringSource()
                .getString(),
            suggestion = Res.string.download_error_reason_server_resume_change_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
