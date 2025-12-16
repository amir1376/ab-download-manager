package com.abdownloadmanager.shared.downloaderinui.hls

import ir.amirab.downloader.downloaditem.hls.HLSDownloadJob
import ir.amirab.downloader.monitor.DurationBasedProcessingDownloadItemState
import ir.amirab.downloader.monitor.UiDurationBasedPart
import ir.amirab.downloader.part.PartDownloadStatus

object UiProcessingItemForHSLFactory {
    fun create(
        downloadJob: HLSDownloadJob,
        speed: Long
    ): DurationBasedProcessingDownloadItemState {
        val item = downloadJob.downloadItem
        val downloadParts = downloadJob.getParts()
        val totalPartsCount = downloadParts.size
        val uiParts = downloadParts.map {
            UiDurationBasedPart.fromPart(
                part = it,
                totalPartsCount = totalPartsCount,
            )
        }
        var finishedPartsCount = 0
        var downloadedBytes = 0L
        // only those who has length
        var activeCount = 0
        var activeCountTotalLength = 0L
        var activeCountProgress = 0L
        // ----------
        for (part in uiParts) {
            val status = part.status
            val howMuchProceed = part.howMuchProceed
            if (status is PartDownloadStatus.Completed) {
                finishedPartsCount++
            } else if (status is PartDownloadStatus.IsActive) {
                val length = part.length
                if (length != null) {
                    activeCount++
                    activeCountProgress += howMuchProceed
                    activeCountTotalLength += length
                }
            }
            downloadedBytes += howMuchProceed
        }
        val percentFraction = getPercentFraction(
            finishedPartsCount = finishedPartsCount,
            totalPartsCount = totalPartsCount,
            activePartsCount = activeCount,
            activeCountsTotalDownloaded = activeCountProgress,
            activeCountsTotalLength = activeCountTotalLength,
        )
        val length = (downloadedBytes / percentFraction).toLong()
        return DurationBasedProcessingDownloadItemState(
            id = downloadJob.id,
            name = item.name,
            folder = item.folder,
            status = downloadJob.status.value,
            speed = speed,
            supportResume = true,
            contentLength = item.contentLength,
            parts = uiParts,
            downloadLink = item.link,
            saveLocation = item.name,
            dateAdded = item.dateAdded,
            startTime = item.startTime ?: 0,
            completeTime = item.completeTime ?: 0,
            duration = item.duration,
            progress = downloadedBytes,
            percent = (percentFraction * 100).toInt(),
            optimisticLength = length,
        )
    }

    private fun getPercentFraction(
        finishedPartsCount: Int,
        totalPartsCount: Int,
        activePartsCount: Int,
        activeCountsTotalDownloaded: Long,
        activeCountsTotalLength: Long,
    ): Double {
        // how much all of active parts have progress (0.0-1.0)
        val activePartCountProgress = if (activeCountsTotalLength == 0L) {
            0.0
        } else {
            (activeCountsTotalDownloaded / activeCountsTotalLength.toDouble()) * activePartsCount
        }
        return (finishedPartsCount + activePartCountProgress) / totalPartsCount
    }
}
