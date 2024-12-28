package ir.amirab.util

fun wildcardMatch(
    pattern: String,
    input: String,
): Boolean {
    return pattern
        .split("*")
        .joinToString(".*") { Regex.escape(it) }
        .toRegex()
        .containsMatchIn(input)
}