import com.abdownloadmanager.updateapplier.UpdateInstaller
import ir.amirab.util.osfileutil.FileUtils
import java.io.File

class ApkInstaller(
    private val apkFile: File,
) : UpdateInstaller {
    override fun installUpdate() {
        FileUtils.openFile(apkFile)
    }
}
