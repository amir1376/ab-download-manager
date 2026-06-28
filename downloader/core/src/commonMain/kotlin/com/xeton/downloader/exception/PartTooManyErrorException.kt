package com.xeton.downloader.exception

import com.xeton.downloader.part.DownloadPart

class PartTooManyErrorException(
    part: DownloadPart,
    override val cause: Throwable
) : Exception(
        "this part $part have too many errors",
    cause,
)
