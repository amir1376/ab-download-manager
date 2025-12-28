package com.abdownloadmanager.android.pages.browser

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class SearchEngines(
    val baseUrl: String,
    val query: String,
    val home: String = baseUrl,
) {
    fun createSearchUrl(textToSearch: String): String {
        return buildSearchUrl(baseUrl, query, textToSearch)
    }

    data object DuckDuckGo : SearchEngines(
        baseUrl = "https://duckduckgo.com/",
        query = "q",
    )

    data object Google : SearchEngines(
        baseUrl = "https://www.google.com/search",
        query = "q",
        home = "https://www.google.com",
    )

    data object Bing : SearchEngines(
        baseUrl = "https://www.bing.com/search",
        query = "q",
    )

    data object Brave : SearchEngines(
        baseUrl = "https://search.brave.com/search",
        query = "q",
        home = "https://search.brave.com",
    )

    companion object {
        private fun buildSearchUrl(
            baseUrl: String,
            queryParam: String,
            query: String
        ): String {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            return "$baseUrl?$queryParam=$encodedQuery"
        }
    }
}
