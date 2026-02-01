import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(MyPlugins.composeBase)
    id(Plugins.Android.library)
}
val ourPackageName = "com.abdownloadmanager.resources"
val propertiesToKotlinTask by tasks.registering(PropertiesToKotlinTask::class) {
    outputDir.set(file("build/tasks/propertiesToKotlinTask"))
    generatedFileName.set("String.kt")
    packageName.set(ourPackageName)
    myStringResourceClass.set("ir.amirab.resources.contracts.MyStringResource")
    propertyFiles.from("src/commonMain/resources/com/abdownloadmanager/resources/locales/en_US.properties")
}
val generateResourceMap by tasks.registering(GenerateResourceMap::class) {
    outputDir.set(file("build/tasks/generateResourceMapTask"))
    generatedFileName.set("ResourceMap.kt")
    packageName.set(ourPackageName)
    baseFolder.set(file("src/commonMain/resources/"))
}
val generateResObject by tasks.registering(GenerateResObject::class) {
    outputDir.set(file("build/tasks/generateResObjectTask"))
    generatedFileName.set("Res.kt")
    packageName.set(ourPackageName)
    dependsOn(propertiesToKotlinTask)
    dependsOn(generateResourceMap)
}

kotlin {
    jvm("desktop")
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    sourceSets {
        commonMain {
            kotlin {
                srcDirs(propertiesToKotlinTask.map { it.outputDir })
                srcDirs(generateResourceMap.map { it.outputDir })
                srcDirs(generateResObject.map { it.outputDir })
            }
            dependencies {
                implementation(libs.compose.ui)
                api(libs.okio.okio)
                implementation(project(":shared:resources:contracts"))
            }
        }
    }
}
android {
    compileSdk = 36
    namespace = "com.abdownloadmanager.resources"
    defaultConfig {
        minSdk = 26
    }
    sourceSets.named("main") {
        resources.srcDir("src/commonMain/resources")
    }
}
abstract class GenerateResObject @Inject constructor(
    project: Project
) : DefaultTask() {

    @get:Input
    val packageName = project.objects.property<String>()

    @get:OutputDirectory
    val outputDir = project.objects.directoryProperty()

    @get:Input
    val generatedFileName = project.objects.property<String>()

    @TaskAction
    fun run() {
        val content = buildString {
            appendLine("package ${packageName.get()}")
            appendLine("object Res {")
            appendLine("  val string = Strings")
            appendLine("  val sourceMap = ResourceMap")
            appendLine("}")
        }
        outputDir.file(generatedFileName).get().asFile.writer().use {
            it.write(content)
        }
    }
}

abstract class GenerateResourceMap @Inject constructor(
    project: Project
) : DefaultTask() {

    @get:Input
    val packageName = project.objects.property<String>()

    @get:InputDirectory
    val baseFolder = project.objects.directoryProperty()

    @get:OutputDirectory
    val outputDir = project.objects.directoryProperty()

    @get:Input
    val generatedFileName = project.objects.property<String>()

    @TaskAction
    fun run() {
        val base = baseFolder.asFile.get()
        val files = base.walkTopDown()
            .filter {
                it.isFile
            }.map {
                it
                    .relativeTo(base).toString()
                    .replace("\\", "/")
            }
        val content = buildString {
            appendLine("package ${packageName.get()}")
            appendLine("object ResourceMap {")
            appendLine("  val files = listOf(")
            val doubleQuotes = "\"".repeat(3)
            for (file in files) {
                appendLine("    $doubleQuotes$file$doubleQuotes,")
            }
            appendLine("  )")
            appendLine("}")
        }
        outputDir.file(generatedFileName).get().asFile.writer().use {
            it.write(content)
        }
    }
}

abstract class PropertiesToKotlinTask @Inject constructor(
    project: Project
) : DefaultTask() {
    @get:InputFiles
    val propertyFiles = project.objects.fileCollection()

    @get:Input
    val packageName = project.objects.property<String>()

    @get:Input
    val myStringResourceClass = project.objects.property<String>()

    @get:OutputDirectory
    val outputDir = project.objects.directoryProperty()

    @get:Input
    val generatedFileName = project.objects.property<String>()

    @TaskAction
    fun run() {
        val properties = Properties()
        propertyFiles.forEach { file ->
            file.inputStream().use { inputStream ->
                properties.load(inputStream)
            }
        }
        val content = createFileString(
            packageName.get(),
            myStringResourceClass.get(),
            properties
        )
        outputDir.file(generatedFileName).get().asFile.writer().use {
            it.write(content)
        }
    }

    private fun createFileString(
        packageName: String,
        myStringResourceClass: String,
        properties: Properties,
    ): String {
        val myStringResourceClassName = myStringResourceClass
            .split(".").last()
        val variableRegex by lazy { "\\{\\{(?<variable>.+?)\\}\\}".toRegex() }
        fun findVariablesOfValue(value: String): List<String> {
            return variableRegex
                .findAll(value)
                .toList()
                .map {
                    it.groups["variable"]!!.value
                }
        }

        fun propertyToCode(key: String, value: String): String {
            val args = findVariablesOfValue(value)
            val defination = "val `$key` = $myStringResourceClassName(\"$key\")"
            if (args.isEmpty()) {
                return defination
            } else {
                val comment = buildString {
                    append("/**\n")
                    append("accepted args:\n")
                    args.forEach { value ->
                        append("@param [$value]\n")
                    }
                    append("*/")
                }
                val argCreatorFunction = buildString {
                    append("fun `${key}_createArgs`(")
                    args.forEachIndexed { index, value ->
                        append("$value: String")
                        if (index != args.lastIndex) {
                            append(", ")
                        }
                    }
                    append(") = ")
                    append("mapOf(")
                    args.forEachIndexed { index, value ->
                        append("\"$value\" to $value")
                        if (index != args.lastIndex) {
                            append(", ")
                        }
                    }
                    append(")")
                }
                return "$defination\n$comment\n$argCreatorFunction"
            }

        }

        return buildString {
            appendLine("@file:Suppress(\"RemoveRedundantBackticks\", \"FunctionName\")")
            appendLine("package $packageName")
            appendLine("import $myStringResourceClass")

            appendLine("object Strings {")
            for (property in properties) {
                val key = property.key.toString()
                val value = property.value.toString()
                val codeLines = propertyToCode(key, value).lines()
                for (line in codeLines) {
                    appendLine("  $line")
                }
            }
            appendLine("}")
        }
    }
}
