package ir.amirab.git_version.core

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersionOrNull
import org.intellij.lang.annotations.Language


class SelectBestSemanticVersion : TagSelector {
    private val regex by lazy {
        "$semanticVersionRegex$$".toRegex()
    }

    private fun GitReference.TagInfo.toVersionOrNull(): Version? {
        return regex.find(shortenName)?.groups?.get("version")?.value?.toVersionOrNull()
    }

    override fun select(
        tags: List<GitReference.TagInfo>
    ): GitReference.TagInfo? {
        return tags
            .map { it to it.toVersionOrNull() }
            .sortedByDescending { it.second }
//            .also {
//                println(it.map { it.second })
//            }
            .firstOrNull()?.first
    }
}

/**
 * Note: add ending manually
 */
@Language("RegExp")
val semanticVersionRegex = """(?<version>(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+(?<buildmetadata>[0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)"""
