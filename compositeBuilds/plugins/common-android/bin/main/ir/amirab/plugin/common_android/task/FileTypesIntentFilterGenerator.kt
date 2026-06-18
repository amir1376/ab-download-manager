package ir.amirab.plugin.common_android.task

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import okio.FileSystem
import okio.Path.Companion.toPath
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

internal abstract class GenerateFileTypesManifest : DefaultTask() {
    @get:InputFile
    val extensionsFile: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    val targetActivity = project.objects.property<String>()

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun generate() {
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        val extensionList = extensionsFile.asFile.get()
            .bufferedReader()
            .lineSequence()
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .filterNot { it.startsWith("#") }
            .toList()
        val templateFile = "ir/amirab/plugin/common_android/AndroidManifest.xml.hbs".toPath()
        val templateContent = FileSystem.RESOURCES.read(templateFile) {
            readUtf8()
        }
        val patterns = generatePatterns(extensionList)
        val handlebars = Handlebars()
        val manifestContent = handlebars.compileInline(templateContent)
            .apply(
                Context.newContext(
                    mapOf(
                        "activityName" to targetActivity.get(),
                        "patterns" to patterns,
                    )
                )
            )
        output.writeText(manifestContent)
    }

    private fun generatePatterns(extensions: List<String>): List<String> {
        return extensions.flatMap { generatePatternsForExtension(it) }
    }
    private fun generatePatternsForExtension(
        extension: String,
        repeat: Int = 4
    ): List<String> {
        val first = """\\.$extension"""
        return buildList{
            add(".*$first")
            repeat(repeat){
                add(".*${"""\\..*""".repeat(it)}$first.*")
            }
        }
    }
}
