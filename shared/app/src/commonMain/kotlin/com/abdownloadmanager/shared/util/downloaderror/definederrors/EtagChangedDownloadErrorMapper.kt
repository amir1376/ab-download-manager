package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.downloader.exception.FileChangedException
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs

object EtagChangedDownloadErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return throwable is FileChangedException.ETagChangedException
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        throwable as FileChangedException.ETagChangedException
        val oldEtag = throwable.oldETag
        val newEtag = throwable.newETag
        return createErrorReason(
            title = Res.string.download_error_reason_changed_etag_title.asStringSource().getString(),
            description = Res.string.download_error_reason_changed_etag_description.asStringSourceWithARgs(
                Res.string.download_error_reason_changed_etag_description_createArgs(
                    oldETag = oldEtag,
                    newETag = newEtag,
                )
            ).getString(),
            suggestion = Res.string.download_error_reason_changed_etag_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
