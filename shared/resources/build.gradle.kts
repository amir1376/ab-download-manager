import java.util.Properties

plugins {
    id(MyPlugins.kotlin)
}

dependencies {
    implementation(project(":shared:resources:contracts"))
}
val propertiesToKotlinTask by tasks.registering(PropertiesToKotlinTask::class) {
    outputDir.set(file("build/tasks/propertiesToKotlinTask"))
    generatedFileName.set("Res.kt")
    packageName.set("com.abdownloadmanager.resources")
    myStringResourceClass.set("ir.amirab.resources.contracts.MyStringResource")
    propertyFiles.from("src/main/resources/com/abdownloadmanager/resources/locales/en_US.properties")
}
tasks.compileKotlin {
    dependsOn(propertiesToKotlinTask)
}
sourceSets {
    main {
        kotlin {
            srcDirs(propertiesToKotlinTask.map { it.outputDir })
        }
    }
}

abstract class PropertiesToKotlinTask @Inject constructor(
    project: Project,
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
        val variableRegex by lazy { "\\{\\{(?<variable>.+)\\}\\}".toRegex() }
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
            append("@file:Suppress(\"RemoveRedundantBackticks\", \"FunctionName\")")
            append("package $packageName\n")
            append("import $myStringResourceClass\n")

            append("object Res {\n")
            append("    object string {\n")
            for (property in properties) {
                val key = property.key.toString()
                val value = property.value.toString()
                val codeLines = propertyToCode(key, value).lines()
                for (line in codeLines) {
                    append("        $line\n")
                }
            }
            append("    }\n")
            append("}\n")
        }
    }
}