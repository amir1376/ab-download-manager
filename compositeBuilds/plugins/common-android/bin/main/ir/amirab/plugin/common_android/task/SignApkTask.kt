package ir.amirab.plugin.common_android.task

import okio.Path.Companion.toPath
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.process.ExecOperations
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import javax.inject.Inject

sealed interface KeystoreContent {
    companion object {
        fun fromUri(
            uriString: String,
        ): KeystoreContent {
            val splitIndex = uriString.indexOf(':')
            if (splitIndex == -1) {
                throw GradleException("Invalid KeystoreContent it should be <type>:<data>")
            }
            val type = uriString.substring(0, splitIndex)
            val data = uriString.substring(splitIndex + 1)
            return when (type) {
                "file" -> {
                    FromFile(File(data))
                }

                "base64" -> {
                    FromBase64(data)
                }

                else -> error("please provide file or Base64 ( base64:abcdefg or file:/path/to/file )")
            }
        }
    }

    fun getContent(): ByteArray
    data class FromFile(private val file: File) : KeystoreContent {
        override fun getContent(): ByteArray {
            return file.readBytes()
        }
    }

    data class FromBase64(private val base64Content: String) : KeystoreContent {
        override fun getContent(): ByteArray {
            return Base64.getDecoder().decode(base64Content)
        }
    }

}

abstract class SignApkTask : DefaultTask() {
    @get:Inject
    internal abstract val execOps: ExecOperations

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDIr: DirectoryProperty

    @get:Input
    abstract val keystoreUri: Property<String>

    @get:Input
    abstract val keystorePassword: Property<String>

    @get:Input
    abstract val keyAlias: Property<String>

    @get:Input
    abstract val keyPassword: Property<String>

    @get:Input
    abstract val platformToolsVersion: Property<String>

    private fun getApkSignerFile(): String {
        val androidHome = System.getenv("ANDROID_HOME")?.toPath()
            ?: throw GradleException("ANDROID HOME environment variable is not set.")
        val dir = androidHome / "build-tools" / platformToolsVersion.get()
        val name = if (OperatingSystem.current().isWindows) {
            "apksigner.bat"
        } else {
            "apksigner"
        }
        return (dir / name).toString()
    }

    @TaskAction
    fun sign() {
        val inputDir = inputDir.get().asFile
        if (!inputDir.exists()) {
            throw IllegalArgumentException("Input APK does not exist: ${inputDir.absolutePath}")
        }
        val outputDir = outputDIr.get().asFile
        val keystorePassword: String = keystorePassword.get()
        val keyPassword: String = keyPassword.get()
        val keyAlias: String = keyAlias.get()
        val tempKeyStoreFile = getKeyStoreFile()
        try {
            inputDir.listFiles().filter {
                it.name.endsWith(".apk")
            }.forEach {
                signSingleApk(
                    inputApk = it,
                    outputApk = outputDir.resolve(it.name),
                    keystoreFile = tempKeyStoreFile,
                    keystorePassword = keystorePassword,
                    keyAlias = keyAlias,
                    keyPassword = keyPassword
                )
            }
        } finally {
            tempKeyStoreFile.delete()
        }
    }

    private fun getKeyStoreFile(): File {
        val keystoreContent = KeystoreContent.fromUri(
            keystoreUri.get(),
        )
        // Decode base64 keystore to temp file
        val tempKeystore = File.createTempFile("keystore", ".jks")
        FileOutputStream(tempKeystore).use { fos ->
            fos.write(keystoreContent.getContent())
        }
        return tempKeystore
    }

    fun signSingleApk(
        inputApk: File,
        outputApk: File,
        keystoreFile: File,
        keystorePassword: String,
        keyAlias: String,
        keyPassword: String,
    ) {
        logger.lifecycle("Signing APK: $inputApk")
        logger.lifecycle("Output APK: $outputApk")

        execOps.exec {
            commandLine(
                getApkSignerFile(),
                "sign",
                "--ks", keystoreFile,
                "--ks-pass", "pass:$keystorePassword",
                "--key-pass", "pass:$keyPassword",
                "--ks-key-alias", keyAlias,
                "--out", outputApk.absolutePath,
                inputApk.absolutePath,
            )
        }

        logger.lifecycle("Signed APK generated at: $outputApk")

    }
}
