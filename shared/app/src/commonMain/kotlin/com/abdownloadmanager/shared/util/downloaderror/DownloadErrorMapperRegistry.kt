package com.abdownloadmanager.shared.util.downloaderror

import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.ifThen


class DownloadErrorMapperRegistry(
    val exceptionMappers: List<DownloadErrorMapper>
) : IDownloadErrorMapperRegistry {

    override fun getReason(throwable: Throwable): DownloadErrorReason? {
        if (ExceptionUtils.isNormalCancellation(throwable)) {
            return null
        }
        val actualThrowable = throwable.ifThen(throwable is TooManyErrorException) {
            throwable.findActualDownloadErrorCause()
        }
        return exceptionMappers
            .asSequence()
            .filter {
                it.accept(actualThrowable)
            }.firstNotNullOfOrNull {
                runCatching {
                    it.getReason(actualThrowable)
                }.getOrNull()
            }
    }
}
