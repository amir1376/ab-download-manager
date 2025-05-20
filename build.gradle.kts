import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull
import ir.amirab.git_version.core.semanticVersionRegex

plugins {
    ir.amirab.`git-version-plugin`
    /**
     * retrieve latest versions of dependencies
     */
    com.github.`ben-manes`.versions
}

val defaultSemVersion = "1.0.0"
val fallBackVersion = "$defaultSemVersion-untagged"

gitVersion {
    on {
        branch(".+") {
            "$defaultSemVersion-${it.refInfo.shortenName}-snapshot"
        }
        tag("v?${semanticVersionRegex}") {
            it.matchResult.groups.get("version")!!.value
        }
        commit {
            "$defaultSemVersion-sha.${it.refInfo.commitHash.take(5)}"
        }
    }
}
//version="0.0.8"
version = (gitVersion.getVersion() ?: fallBackVersion).toVersion()
logger.lifecycle("version: $version")

tasks.dependencyUpdates {
    revision = "release"
    outputFormatter = "html"
    rejectVersionIf {
        val candidateVersion = candidate.version.toVersionOrNull() ?: return@rejectVersionIf true
        !candidateVersion.isStable
    }
}
