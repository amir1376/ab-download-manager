package ir.amirab.util

fun wildcardMatch(
    pattern: String,
    input: String,
): Boolean {
    return pattern
        .split("*")
        .joinToString(".*") { Regex.escape(it) }
        .toRegex(RegexOption.IGNORE_CASE)
        .containsMatchIn(input)
}
