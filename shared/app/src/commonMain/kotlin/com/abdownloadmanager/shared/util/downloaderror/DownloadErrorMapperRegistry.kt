package com.abdownloadmanager.shared.util.downloaderror

import com.xeton.downloader.exception.TooManyErrorException
import com.xeton.downloader.utils.ExceptionUtils
import com.xeton.util.ifThen


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
