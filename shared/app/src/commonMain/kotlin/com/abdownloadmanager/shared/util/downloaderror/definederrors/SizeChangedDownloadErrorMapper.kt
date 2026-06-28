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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object SizeChangedDownloadErrorMapper : DownloadErrorMapper, KoinComponent {
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider by inject()
    override fun accept(throwable: Throwable): Boolean {
        return throwable is FileChangedException.LengthChangedException
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        throwable as FileChangedException.LengthChangedException

        val oldSize =
            convertPositiveSizeToHumanReadable(
                throwable.lastContentLength,
                sizeAndSpeedUnitProvider.sizeUnit.value,
            )
        val newSize = convertPositiveSizeToHumanReadable(
            throwable.newContentLength,
            sizeAndSpeedUnitProvider.sizeUnit.value,
        )
        return createErrorReason(
            title = Res.string.download_error_reason_changed_size_title.asStringSource().getString(),
            description = Res.string.download_error_reason_changed_size_description.asStringSourceWithARgs(
                Res.string.download_error_reason_changed_size_description_createArgs(
                    oldSize = oldSize.getString(),
                    newSize = newSize.getString(),
                )
            ).getString(),
            suggestion = Res.string.download_error_reason_changed_size_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
