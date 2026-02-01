import buildlogic.CiDirs
import buildlogic.CiUtils
import buildlogic.versioning.convertToVersionCode
import buildlogic.versioning.getAppName
import buildlogic.versioning.getAppVersion
import buildlogic.versioning.getAppVersionString
import buildlogic.versioning.getApplicationPackageName
import com.android.build.api.artifact.SingleArtifact
import ir.amirab.installer.InstallerTargetFormat
import ir.amirab.plugin.common_android.task.SignApkTask
import ir.amirab.plugin.common_android.task.androidEnableFileTypesGeneratorForManifest
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id(Plugins.Android.application)
    id(MyPlugins.kotlinAndroid)
    id(MyPlugins.composeAndroid)
    id(Plugins.ksp)
    id(Plugins.Kotlin.serialization)
    id(Plugins.aboutLibraries)
    id(Plugins.aboutLibrariesAndroid)
}
android {
    defaultConfig {
        minSdk = 26
        targetSdk = 36
        applicationId = getApplicationPackageName()
        versionCode = getAppVersion().convertToVersionCode()
        versionName = getAppVersionString()
    }
    compileSdk = 36
    namespace = "com.abdownloadmanager.android"
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_short_name", "AB DM - Debug")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
kotlin.compilerOptions {
    jvmTarget = JvmTarget.JVM_21
}
dependencies {
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.decompose.jbCompose)
    implementation(libs.aboutLibraries.core)
    implementation(project(":shared:app"))
    ksp(libs.arrow.opticKsp)
}

androidEnableFileTypesGeneratorForManifest(
    targetActivityClass = ".pages.add.AddDownloadActivity",
    fileTypesFile = project.layout.projectDirectory.file("filetypes.txt")
)


// ======= begin of GitHub action stuff
val ciDir = CiUtils.getCiDir(project)
androidComponents.onVariants { variant ->
    tasks.register(
        "createReleaseSignedBinary${variant.name.uppercaseFirstChar()}",
        SignApkTask::class
    ) {
        inputDir.set(variant.artifacts.get(SingleArtifact.APK))
        outputDIr.set(project.layout.buildDirectory.dir("generatedSignedApks"))
        platformToolsVersion.set("36.1.0")
        keystoreUri.set(provider {
            getFromEnvOrProperties("ABDM_KEYSTORE_FILE")
        })
        keystorePassword.set(provider {
            getFromEnvOrProperties("ABDM_KEYSTORE_FILE_PASSWORD")
        })
        keyPassword.set(provider {
            getFromEnvOrProperties("ABDM_KEYSTORE_KEY_PASSWORD")
        })
        keyAlias.set(provider {
            getFromEnvOrProperties("ABDM_KEYSTORE_KEY_ALIAS")
        })
    }
}

val androidBinaries by tasks.registering {
    val signedApks = tasks.named("createReleaseSignedBinaryRelease")
        .map { task ->
            task.outputs.files.singleFile
        }
    inputs.dir(signedApks)
    outputs.dir(ciDir.binariesDir)
    doLast {
        // at the moment we only have one apk
        // if I decided to add multiple targets (arm64 x64 etc..)
        // ... I need to extract arch and use forEach instead of first
        val signedApk = signedApks.get().listFiles()
            .first { it.name.endsWith(".apk") }
        val outputFileName = CiUtils.getTargetFileName(
            getAppName(),
            getAppVersion(),
            InstallerTargetFormat.Apk,
            null,
        )
        CiUtils.copyAndHashToDestination(
            src = signedApk,
            destinationFolder = ciDir.binariesDir.get().asFile,
            name = outputFileName,
        )
    }
}

tasks.register(CiUtils.getCreateBinaryFolderForCiTaskName()) {
    dependsOn(androidBinaries)
}


private val localProperties by lazy {
    val file = project.rootProject.projectDir.resolve("local.properties")
    file.inputStream().use {
        Properties().apply { load(it) }
    }
}

fun getFromEnvOrProperties(key: String): String? {
    val string = (System.getenv(key)?.takeIf { it.isNotEmpty() }
        ?: localProperties.getProperty(key))
    return string
}
