package com.abdownloadmanager.shared.util.extractors.linkextractor

import com.abdownloadmanager.shared.util.extractors.Extractor
import ir.amirab.util.HttpUrlUtils

object StringUrlExtractor : Extractor<String, List<String>> {
    private val urlRegex by lazy { Regex("""\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]""") }
    override fun extract(input: String): List<String> {
        // maybe each line is a link
        val linksInEachLines = byEachLine(input)
        if (linksInEachLines.isNotEmpty()) {
            return linksInEachLines
        }
        // try to find links by regex
        return byRegex(input)
    }

    private fun byEachLine(input: String): List<String> {
        return input
            .lineSequence()
            .map { it.trim() }
            .filter { HttpUrlUtils.isValidUrl(it) }
            .toList()
    }

    private fun byRegex(input: String): List<String> {
        return urlRegex.findAll(input)
            .map { it.value }
            .filter { HttpUrlUtils.isValidUrl(it) }
            .toList()
    }
}
