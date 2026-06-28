package com.abdownloadmanager.shared.util.downloaderror

import com.abdownloadmanager.shared.util.downloaderror.definederrors.*

class DownloadErrorMapperRegistryFactory {
    private fun getAvailableMappers(): List<DownloadErrorMapper> {
        return listOf(
            HttpStatusDownloadErrorMapper,
            UnknownHostErrorMapper,
            ChangedToWebPageDownloadErrorMapper,
            ConnectionResetDownloadErrorMapper,
            DestinationExceptionDownloadErrorMapper,
            EtagChangedDownloadErrorMapper,
            SizeChangedDownloadErrorMapper,
            ResumeSupportChangedDownloadErrorMapper,
            TimeoutErrorMapper,
            SSLNotTrustedErrorMapper,
            NotEnoughStorageAvailableExceptionMapper(),
            // at last
            DefaultDownloadErrorMapper,
        )
    }


    fun createRegistry(): IDownloadErrorMapperRegistry {
        return DownloadErrorMapperRegistry(
            getAvailableMappers()
        )
    }
}
