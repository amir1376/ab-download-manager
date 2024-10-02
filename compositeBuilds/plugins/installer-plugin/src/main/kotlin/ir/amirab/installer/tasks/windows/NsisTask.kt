package ir.amirab.installer.tasks.windows

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import ir.amirab.installer.extensiion.WindowsConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import java.io.ByteArrayInputStream
import java.io.File

abstract class NsisTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val destFolder: DirectoryProperty

    @get:Input
    abstract val outputFileName: Property<String>

    @get:InputFile
    abstract val nsisTemplate: Property<File>

    @get:Input
    abstract val commonParams: Property<WindowsConfig>

    @get:Input
    val extraParams: MapProperty<String, Any> = project.objects.mapProperty<String, Any>()

    @get:Internal
    abstract val nsisExecutable: Property<File>

    init {
        nsisExecutable.convention(
            project.provider { File("C:\\Program Files (x86)\\NSIS\\makensis.exe") }
        )
    }

    private fun createHandleBarContext(): Context {
        val commonParams = commonParams.get()
        val common = mapOf(
            "app_name" to commonParams.appName!!,
            "app_display_name" to commonParams.appDisplayName!!,
            "app_version" to commonParams.appVersion!!,
            "app_display_version" to commonParams.appDisplayVersion!!,
            "license_file" to commonParams.licenceFile!!,
            "icon_file" to commonParams.iconFile!!,
        )
        val overrides = mapOf(
            "input_dir" to sourceFolder.get().asFile.absolutePath,
            "output_file" to "${destFolder.file(outputFileName).get().asFile.path}.exe",
        )
        return Context.newContext(
            extraParams
                .get()
                .plus(common)
                .plus(overrides)
        )
    }

    @TaskAction
    fun run() {
        val executable = nsisExecutable.get()
        val scriptTemplate = nsisTemplate.get()
        val handlebars = Handlebars()
        val context = createHandleBarContext()
        val script = handlebars.compileInline(
            scriptTemplate.readText()
        ).apply(context)
        logger.debug("NSIS Script:")
        logger.debug(script)
        project.exec {
            executable(
                executable,
            )
            args("-")
            standardInput = ByteArrayInputStream(script.toByteArray())

        }
    }
}