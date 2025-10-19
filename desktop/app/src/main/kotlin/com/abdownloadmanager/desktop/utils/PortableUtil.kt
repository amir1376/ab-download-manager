package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.SharedConstants
import java.io.File

object PortableUtil {
    fun getPortableDataDir(installationFolder: String?): File? {
        if (installationFolder != null) {
            getDefaultPortableFolder(installationFolder)
                ?.let { return it }
            getCustomPortableFolder(installationFolder)
                ?.let { return it }
        }
        return null
    }


    private const val PORTABLE_FILE_NAME = ".portable"

    private fun getDefaultPortableFolder(
        installationFolder: String
    ): File? {
        val dataDirName = SharedConstants.dataDirName
        val portableDataDir = File(installationFolder, dataDirName)
        return portableDataDir.takeIf {
            it.exists() && it.canWrite()
        }
    }

    private fun getCustomPortableFolder(
        installationFolder: String,
    ): File? {
        val portableFile = File(installationFolder, PORTABLE_FILE_NAME)
            .takeIf { it.exists() && it.canRead() }
            ?: return null
        try {
            val customPortableDirText = portableFile
                .readText()
                .trim()
                .takeIf { it.isNotEmpty() } ?: error("$PORTABLE_FILE_NAME file is empty")
            val absoluteFile = getAbsoluteFile(
                baseFile = installationFolder,
                maybeRelative = customPortableDirText
            )
            // make sure it can be used
            return absoluteFile.canonicalFile
        } catch (e: Exception) {
            System.err.println("getting custom portable path failed")
            e.printStackTrace()
            return null
        }
    }

    private fun getAbsoluteFile(
        baseFile: String,
        maybeRelative: String
    ): File {
        val file = File(maybeRelative)
        return if (file.isAbsolute) {
            file
        } else {
            File(baseFile, maybeRelative)
        }
    }
}
