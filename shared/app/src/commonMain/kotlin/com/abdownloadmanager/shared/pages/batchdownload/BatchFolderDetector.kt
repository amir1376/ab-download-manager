package com.abdownloadmanager.shared.pages.batchdownload

import com.abdownloadmanager.shared.util.FilenameFixer
import ir.amirab.util.HttpUrlUtils

object BatchFolderDetector {

    private val genericDirectoryNames = setOf(
        "files", "file", "downloads", "download",
        "uploads", "upload", "media", "public",
        "static", "assets", "content", "data", "pnot", "series"
    )

    private val seasonRegex = Regex("""^(?:S\d+|Season\s*\d+)$""", RegexOption.IGNORE_CASE)

    fun detect(urls: List<String>): String {
        if (urls.isEmpty()) {
            return ""
        }

        val parsed = urls.mapNotNull { urlStr ->
            runCatching {
                val httpUrl = HttpUrlUtils.createURL(urlStr)
                val host = httpUrl.host
                val segments = httpUrl.pathSegments.filter { it.isNotBlank() }
                val filename = HttpUrlUtils.extractNameFromLink(urlStr).orEmpty()
                ParsedUrlInfo(host, segments, filename)
            }.getOrNull()
        }

        if (parsed.isEmpty()) {
            return "Batch_Downloads"
        }

        val filenames = parsed.map { it.filename }.filter { it.isNotBlank() }

        // Strategy 1: Filename Longest Common Prefix
        if (filenames.size == parsed.size && filenames.isNotEmpty()) {
            if (filenames.size == 1) {
                val singleName = filenames.first().substringBeforeLast('.').trim()
                if (singleName.isNotEmpty()) {
                    return FilenameFixer.fix(singleName)
                }
            } else {
                val prefix = longestCommonPrefix(filenames)
                if (prefix.length >= 3) {
                    val trimmed = prefix.trimEnd('.', '-', '_', ' ')
                    val chosen = if (trimmed.length >= 3) trimmed else prefix
                    return FilenameFixer.fix(chosen)
                }
            }
        }

        // Strategy 2: Common path directories
        val dirSegmentsList = parsed.mapNotNull {
            if (it.segments.size >= 2) it.segments.dropLast(1) else null
        }
        if (dirSegmentsList.size == parsed.size && dirSegmentsList.isNotEmpty()) {
            val commonDirs = mutableListOf<String>()
            val minLen = dirSegmentsList.minOf { it.size }
            for (i in 0 until minLen) {
                val seg = dirSegmentsList.first()[i]
                if (dirSegmentsList.all { it[i] == seg }) {
                    commonDirs.add(seg)
                } else {
                    break
                }
            }

            for (i in commonDirs.indices.reversed()) {
                val seg = commonDirs[i]
                if (seg.lowercase() !in genericDirectoryNames && seg.length >= 2) {
                    // If season folder like S03 and previous segment is series title
                    if (seasonRegex.matches(seg) && i > 0) {
                        val prev = commonDirs[i - 1]
                        if (prev.lowercase() !in genericDirectoryNames) {
                            return FilenameFixer.fix("$prev.$seg")
                        }
                    }
                    return FilenameFixer.fix(seg)
                }
            }
        }

        // Strategy 3: Common host / domain
        val hosts = parsed.map { it.host }.filter { it.isNotBlank() }.distinct()
        if (hosts.size == 1) {
            var host = hosts.first()
            if (host.startsWith("www.", ignoreCase = true)) {
                host = host.substring(4)
            }
            if (host.isNotBlank()) {
                return FilenameFixer.fix("Batch_$host")
            }
        }

        // Strategy 4: Fallback for heterogeneous URLs
        return "Batch_Downloads"
    }

    fun longestCommonPrefix(strings: List<String>): String {
        if (strings.isEmpty()) return ""
        var prefix = strings[0]
        for (i in 1 until strings.size) {
            val s = strings[i]
            var j = 0
            while (j < prefix.length && j < s.length && prefix[j] == s[j]) {
                j++
            }
            prefix = prefix.substring(0, j)
            if (prefix.isEmpty()) break
        }
        return prefix
    }

    private data class ParsedUrlInfo(
        val host: String,
        val segments: List<String>,
        val filename: String,
    )
}
