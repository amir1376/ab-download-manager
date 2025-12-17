package ir.amirab.plugin.common_android.task

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register

private fun Project.androidComponents(configure: Action<com.android.build.api.variant.ApplicationAndroidComponentsExtension>): Unit =
    (this as ExtensionAware).extensions.configure("androidComponents", configure)


fun Project.androidEnableFileTypesGeneratorForManifest(
    targetActivityClass: String,
    fileTypesFile: RegularFile,
) {
    androidComponents {
        onVariants{variant ->
            val taskName = "generate${variant.name.capitalized()}FileTypesManifest"
            val task = tasks.register<GenerateFileTypesManifest>(taskName) {
                extensionsFile.set(fileTypesFile)
                targetActivity.set(targetActivityClass)
            }
            variant.sources.manifests.addGeneratedManifestFile(task, GenerateFileTypesManifest::outputFile)
        }
    }
}
