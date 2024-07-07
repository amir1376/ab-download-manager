package ir.amirab.downloader.exception

import ir.amirab.downloader.part.Part

class PartTooManyErrorException(
    part: Part
) : Exception("this part $part have too many exception")
