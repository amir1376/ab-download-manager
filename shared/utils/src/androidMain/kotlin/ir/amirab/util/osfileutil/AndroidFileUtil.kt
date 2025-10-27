package ir.amirab.util.osfileutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class AndroidFileUtil : FileUtilsBase(), KoinComponent {
    val context: Context by inject()
    override fun openFileInternal(file: File): Boolean {
        val mimeType = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(file.extension.lowercase())
            ?: "*/*"


        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }
            .onFailure {
                it.printStackTrace()
                (it.localizedMessage ?: it::class.qualifiedName)?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            .getOrElse { false }
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return file.parentFile?.let {
            openFolderInternal(it)
        } ?: false
    }

    override fun openFolderInternal(folder: File): Boolean {
        throw UnsupportedOperationException(
            "Android doesn't support open folder"
        )
    }

    override fun isRemovableStorage(path: String): Boolean {
        return false
    }

}
