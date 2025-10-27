package ir.amirab.downloader.exception

import ir.amirab.downloader.part.DownloadPart

class PartTooManyErrorException(
    part: DownloadPart,
    override val cause: Throwable
) : Exception(
        "this part $part have too many errors",
    cause,
)
