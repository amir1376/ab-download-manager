package ir.amirab.util.compose.localizationmanager

import arrow.core.fold

private fun String.replaceWithVariable(name: String, value: String): String {
    return replace("{{$name}}", value)
}

internal fun String.withReplacedArgs(args: Map<String, String>): String {
    return args.fold(this) { acc, entry ->
        acc.replaceWithVariable(entry.key, entry.value)
    }
}