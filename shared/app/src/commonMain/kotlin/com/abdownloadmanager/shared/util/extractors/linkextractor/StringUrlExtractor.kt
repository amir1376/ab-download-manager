package com.abdownloadmanager.shared.util.extractors.linkextractor

import com.abdownloadmanager.shared.util.extractors.Extractor
import ir.amirab.util.HttpUrlUtils

object StringUrlExtractor : Extractor<String, List<String>> {
    private val urlRegex by lazy {
        Regex("""\b(?:https?|ftp|file)://[^\s<>"'`]+""")
    }

    override fun extract(input: String): List<String> {
        val lineLinks = byEachLine(input)
        val regexLinks = byRegex(input)
        val result = LinkedHashSet<String>()
        result.addAll(lineLinks)
        result.addAll(regexLinks)
        return result.toList()
    }

    private fun byEachLine(input: String): List<String> {
        return input
            .lineSequence()
            .map { it.trim().trim('"', '\'', '<', '>', '`', '(', ')') }
            .filter { it.isNotBlank() && HttpUrlUtils.isValidUrl(it) }
            .toList()
    }

    private fun byRegex(input: String): List<String> {
        return urlRegex.findAll(input)
            .map { cleanUrl(it.value) }
            .filter { it.isNotBlank() && HttpUrlUtils.isValidUrl(it) }
            .toList()
    }

    private fun cleanUrl(raw: String): String {
        var url = raw.trim()
        val trailingPunctuation = charArrayOf(')', ']', '}', '>', '"', '\'', ',', ';', '.')
        while (url.isNotEmpty() && url.last() in trailingPunctuation && !HttpUrlUtils.isValidUrl(url)) {
            url = url.dropLast(1)
        }
        return url
    }
}
