import buildlogic.CiUtils
import buildlogic.versioning.getAppVersionString
import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull
import ir.amirab.git_version.core.semanticVersionRegex
import org.jetbrains.changelog.Changelog

plugins {
    ir.amirab.`git-version-plugin`
    /**
     * retrieve latest versions of dependencies
     */
    com.github.`ben-manes`.versions
    id(Plugins.changeLog)
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

// ======= begin of GitHub action stuff

val ciDir = CiUtils.getCiDir(project)
changelog {
    path.set(rootProject.layout.projectDirectory.dir("CHANGELOG.md").asFile.path)
    version.set(getAppVersionString())
}
val createChangeNoteForCi by tasks.registering {
    inputs.property("appVersion", getAppVersionString())
    inputs.file(changelog.path)
    outputs.file(ciDir.changeNotesFile)
    doLast {
        val output = ciDir.changeNotesFile.get().asFile
        val bodyText = with(changelog) {
            getOrNull(getAppVersionString())?.let { item ->
                renderItem(item, Changelog.OutputType.MARKDOWN)
            }
        }.orEmpty()
        logger.lifecycle("changeNotes written in $output")
        output.writeText(bodyText)
    }
}

val createReleaseFolderForCi by tasks.registering {
    val createBinariesForCi = CiUtils.getCreateBinaryFolderForCiTaskName()
    dependsOn("desktop:app:$createBinariesForCi")
    val skipAndroidBuild = System.getenv("SKIP_ANDROID_BUILD")
        ?.toBoolean()
        ?: false
    if (!skipAndroidBuild) {
        dependsOn("android:app:$createBinariesForCi")
    }
    val shouldGenerateChangelog = true
    if (shouldGenerateChangelog) {
        dependsOn(createChangeNoteForCi)
    }
}

// ======= end of GitHub action stuff
