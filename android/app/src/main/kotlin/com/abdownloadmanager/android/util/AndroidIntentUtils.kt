package com.abdownloadmanager.android.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object AndroidIntentUtils {
    fun shareFiles(context: Context, files: List<File>) {
        if (files.isEmpty()) return

        val uris = files.map { file ->
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }

        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "*/*" // You can detect type from file extension if you want
                putExtra(Intent.EXTRA_STREAM, uris[0])
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

}
