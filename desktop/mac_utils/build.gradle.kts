import buildlogic.HashUtils
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac

plugins {
    id(MyPlugins.kotlin)
}

val generatedResources = layout.buildDirectory.dir("generated/resources")

val setsidSource = layout.projectDirectory.file("src/main/native/setsid.c")
val setsidBinary = generatedResources.map { it.file("native/macos/setsid") }
val setsidHash = generatedResources.map { it.file("native/macos/setsid.sig") }

val compileMacOsSetsid = tasks.register<Exec>("compileMacOsSetsid") {
    description = "Compile the macOS setsid helper"
    onlyIf { Platform.isMac() }

    inputs.file(setsidSource)
    outputs.file(setsidBinary)

    doFirst {
        setsidBinary.get().asFile.parentFile.mkdirs()
    }

    commandLine(
        "clang",
        setsidSource.asFile.absolutePath,
        "-O2",
        "-Wall",
        "-Wextra",
        "-Werror",
        "-arch", "arm64",
        "-arch", "x86_64",
        // same minimum as the app launcher, otherwise clang uses the build machine's macOS version
        "-mmacosx-version-min=10.13",
        "-o", setsidBinary.get().asFile.absolutePath,
    )
}

val generateMacOsSetsidHash = tasks.register("generateMacOsSetsidHash") {
    description = "Generate SHA-256 hash for the macOS setsid helper"
    onlyIf { Platform.isMac() }
    dependsOn(compileMacOsSetsid)

    inputs.file(setsidBinary)
    outputs.file(setsidHash)

    doLast {
        val binaryFile = setsidBinary.get().asFile
        if (binaryFile.exists()) {
            val hash = HashUtils.sha256(binaryFile)
            val hashFile = setsidHash.get().asFile
            hashFile.parentFile.mkdirs()
            hashFile.writeText(hash.trim())
        }
    }
}

tasks.named("processResources") {
    dependsOn(compileMacOsSetsid)
    dependsOn(generateMacOsSetsidHash)
}

sourceSets {
    main {
        resources.srcDir(generatedResources)
    }
}
