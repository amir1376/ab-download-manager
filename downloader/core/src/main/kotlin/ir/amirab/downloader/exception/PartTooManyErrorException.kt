package ir.amirab.downloader.exception

import ir.amirab.downloader.part.Part

class PartTooManyErrorException(
    part: Part,
    override val cause: Throwable
) : Exception(
        "this part $part have too many errors",
    cause,
)
