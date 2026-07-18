package com.abdownloadmanager.desktop.utils.singleInstance.service

import com.abdownloadmanager.integration.AddDownloadOptionsFromIntegration
import com.abdownloadmanager.integration.ApiQueueModel
import com.abdownloadmanager.integration.NewDownloadTask
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.utils.ExceptionUtils
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Rpc
interface IDefaultAppIPCService {
    // download
    suspend fun addDownload(request: AddDownloadFromIPC)
    suspend fun pauseDownload(ids: List<Long>)
    suspend fun resumeDownload(ids: List<Long>)
    suspend fun removeDownload(ids: List<Long>, alsoRemoveFile: Boolean)
    suspend fun showDownload(ids: List<Long>): List<ShowDownloadIPC>

    suspend fun listQueues(): List<ApiQueueModel>
}

@Serializable
data class AddDownloadFromIPC(
    val items: List<NewDownloadTask>,
    val options: AddDownloadOptionsFromIntegration = AddDownloadOptionsFromIntegration()
)

@Serializable
data class ShowDownloadIPC(
    val name: String,
    val folder: String,
    val id: Long,
    val percent: Int?,
    val status: DownloadStatus
) {
    @Serializable
    enum class DownloadStatus {
        Paused,
        Error,
        Downloading,
        Finished,
        PreparingFile,
        Resuming,
        Retrying;

        companion object {
            fun fromDownloadStatus(status: DownloadJobStatus): DownloadStatus {
                return when (status) {
                    is DownloadJobStatus.Canceled -> {
                        if (ExceptionUtils.isNormalCancellation(status.e)) {
                            Paused
                        } else {
                            Error
                        }
                    }
                    DownloadJobStatus.Downloading -> Downloading
                    DownloadJobStatus.Finished -> Finished
                    DownloadJobStatus.IDLE -> Paused
                    is DownloadJobStatus.PreparingFile -> PreparingFile
                    DownloadJobStatus.Resuming -> Resuming
                    is DownloadJobStatus.Retrying -> Retrying
                }
            }
        }
    }
}
