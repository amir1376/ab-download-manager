package com.abdownloadmanager.android.pages.directorypicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContract
import com.abdownloadmanager.android.util.activity.ABDMActivity
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import okio.Path
import okio.Path.Companion.toPath

class DirectoryPickerActivity : ABDMActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = DirectoryPickerActivity.getTitle(intent).orEmpty().asStringSource()
        val initialDirectory = (
                DirectoryPickerActivity.getInitialDirectory(intent)
                // default if there is no directory provided to us
                    ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                ).toPath()

        setABDMContent {
            DirectoryPicker(
                title = title,
                isVisible = true,
                initialDirectory = initialDirectory,
                onDirectorySelected = {
                    returnTheActivityResult(it)
                },
            )
        }
    }

    private fun returnTheActivityResult(path: Path?) {
        if (path == null) {
            setResult(RESULT_CANCELED)
        } else {
            setResult(RESULT_OK, Intent().putExtra(DIRECTORY_RESULT, path.toString()))
        }
        finish()
    }

    data class Inputs(
        val title: StringSource,
        val initialDirectory: Path?,
    )

    companion object {
        const val TITLE_KEY = "title"
        const val INITIAL_DIR_KEY = "initialDirectory"
        const val DIRECTORY_RESULT = "directory"

        val Contract = object : ActivityResultContract<Inputs, Path?>() {
            override fun createIntent(
                context: Context,
                input: Inputs
            ): Intent {
                return Intent(context, DirectoryPickerActivity::class.java).apply {
                    putExtra(TITLE_KEY, input.title.getString())
                    putExtra(INITIAL_DIR_KEY, input.initialDirectory.toString())
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Path? {
                return if (resultCode == RESULT_OK) {
                    intent?.getStringExtra(DIRECTORY_RESULT)?.toPath()
                } else null
            }

        }


        fun getTitle(intent: Intent): String? {
            return intent.getStringExtra(TITLE_KEY)
        }

        fun getInitialDirectory(intent: Intent): String? {
            return intent.getStringExtra(INITIAL_DIR_KEY)
        }
    }
}
