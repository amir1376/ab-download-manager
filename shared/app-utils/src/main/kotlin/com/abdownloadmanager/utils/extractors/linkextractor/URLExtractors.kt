package com.abdownloadmanager.utils.extractors.linkextractor

import com.abdownloadmanager.utils.extractors.Extractor
import com.abdownloadmanager.utils.isValidUrl

object StringUrlExtractor: Extractor<String, List<String>> {
    private val urlRegex = Regex("""\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]""")
    override fun extract(input: String):List<String>{
        return urlRegex.findAll(input)
            .map { it.value }
            .filter { isValidUrl(it) }
            .toList()
    }
}