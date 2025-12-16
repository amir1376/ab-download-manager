package com.abdownloadmanager.android.pages.crashreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.abdownloadmanager.android.util.activity.ABDMActivity

class CrashReportActivity : ABDMActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val throwableData = getExceptionData(intent)
        setABDMContent {
            ErrorWindow(throwableData) {
                finish()
            }
        }
    }

    private fun getExceptionData(intent: Intent): ThrowableData {
        return ThrowableData(
            intent.getStringExtra(TITLE_KEY).orEmpty(),
            intent.getStringExtra(STACKTRACE_KEY).orEmpty(),
        )
    }

    companion object {
        private const val TITLE_KEY = "title"
        private const val STACKTRACE_KEY = "stacktrace"
        fun createIntent(
            context: Context,
            throwable: Throwable
        ): Intent {
            val throwableData = ThrowableData.fromThrowable(throwable)
            return Intent(
                context,
                CrashReportActivity::class.java
            ).apply {
                putExtra(TITLE_KEY, throwableData.title)
                putExtra(STACKTRACE_KEY, throwableData.stacktrace)
            }
        }
    }
}
