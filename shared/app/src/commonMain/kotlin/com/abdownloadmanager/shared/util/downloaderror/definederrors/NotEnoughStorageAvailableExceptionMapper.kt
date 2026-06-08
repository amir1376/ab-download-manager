package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import ir.amirab.downloader.exception.NoSpaceInStorageException
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotEnoughStorageAvailableExceptionMapper : DownloadErrorMapper, KoinComponent {
    private val sizeUnitProvider: SizeAndSpeedUnitProvider by inject()
    override fun accept(throwable: Throwable): Boolean {
        return throwable is NoSpaceInStorageException
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        throwable as NoSpaceInStorageException
        val required = convertPositiveSizeToHumanReadable(
            throwable.required,
            sizeUnitProvider.sizeUnit.value,
        ).getString()
        val available = convertPositiveSizeToHumanReadable(
            throwable.available,
            sizeUnitProvider.sizeUnit.value,
        ).getString()

        return createErrorReason(
            title = Res.string.download_error_reason_no_space_title.asStringSource().getString(),
            description = Res.string.download_error_reason_no_space_description.asStringSourceWithARgs(
                Res.string.download_error_reason_no_space_description_createArgs(
                    required = required,
                    available = available,
                )
            ).getString(),
            suggestion = Res.string.download_error_reason_no_space_suggestion.asStringSource().getString(),
            throwable = throwable,
        )
    }
}
