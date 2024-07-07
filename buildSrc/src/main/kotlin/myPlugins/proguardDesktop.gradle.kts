package myPlugins

import org.jetbrains.compose.desktop.application.tasks.AbstractProguardTask
import java.util.zip.ZipFile

plugins {
    id("org.jetbrains.compose")
}

fun getProguardFileContent(file: File): List<Pair<String, String>> {
    val list = ArrayList<Pair<String, String>>()
    if (file.isFile) {
        if (file.name.endsWith(".jar", true)) {
            return runCatching {
                val zipFile = ZipFile(file)
                list.apply {
                    addAll(zipFile.use { zFile ->
                        zFile.entries().toList().filter {
                            it.name.run {
                                endsWith(".pro")
//                                        && (startsWith("META-INF/proguard") || !contains("/"))
                            }
                        }.map {
                            val name = it.name.split("/").last()
                            val content = zFile.getInputStream(it).reader().use { it.readText() }
                            name to """
# - rules applied from $name                                    
$content                                
                            """.trimIndent()
                        }
                    })
                }
            }.getOrThrow()
        }
    }
    return list
}
val compileClasspathProvider = configurations.named("compileClasspath")
val getProguardConfigurations by tasks.registering {
    dependsOn(compileClasspathProvider)
    val folder = layout.buildDirectory.map {
        it.dir("resolvedProguards")
    }
    inputs.files(compileClasspathProvider)
    outputs.dir(folder)
    doLast {
        val outputFolder = folder.get().asFile
        outputFolder.deleteRecursively()
        compileClasspathProvider.get().files.forEach { file ->
            val outPutOfPackage = outputFolder.resolve("${file.name}")
            for ((name, content) in getProguardFileContent(file)) {
                outPutOfPackage
                    .resolve(name).also {
                        it.parentFile.mkdirs()
                    }
                    .writeText(content)
            }
        }
    }
}
tasks.withType<AbstractProguardTask> {
    dependsOn(getProguardConfigurations)
}