package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.downloader.exception.FileChangedException
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs

object ChangedToWebPageDownloadErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return throwable is FileChangedException.GotAWebPage
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        return createErrorReason(
            title = Res.string.download_error_reason_changed_web_title.asStringSource().getString(),
            description = Res.string.download_error_reason_changed_web_description.asStringSource().getString(),
            suggestion = Res.string.download_error_reason_changed_web_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
