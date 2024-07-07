package com.abdownloadmanager.updateapplier;

import ir.amirab.util.FileUtils
import com.abdownloadmanager.updatechecker.VersionData
import java.io.File
interface UpdateDownloader{
    suspend fun download(link:String):File
}
class JavaUpdateApplier(
    private val versionData: VersionData,
    private val updateDownloader: UpdateDownloader
) : UpdateApplier() {
    private var downloading: Boolean = false

    override suspend fun applyUpdate() {
        //it is only check for same instance
        // if I faced to multiple update (when user press "update" many times)
        // I have to cancel this suspension job and create a new instance instead
        if (downloading){
            return
        }
        downloading=true

        val executableFile = updateDownloader.download(versionData.link)
        if (!executableFile.exists() || !executableFile.canExecute()){
            downloading=false
            return
        }
        //TODO investigate on possible security risks
        FileUtils.openFile(executableFile)
    }
}
