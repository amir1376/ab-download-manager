package com.abdownloadmanager.desktop

import com.abdownloadmanager.desktop.ui.Ui
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.GlobalAppExceptionHandler
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

class LanguageSupportTest {

    @Test
    fun testChineseInterface() {
        val appArguments = AppArguments(arrayOf("--language", "zh"))
        val globalAppExceptionHandler = GlobalAppExceptionHandler()

        runBlocking {
            Ui.boot(appArguments, globalAppExceptionHandler)
        }

        val resourceBundle = ResourceBundle.getBundle("strings/strings", Locale("zh"))
        val expectedTitle = resourceBundle.getString("app.title")

        assert(AppInfo.name == expectedTitle) { "Expected: $expectedTitle, but got: ${AppInfo.name}" }
    }

    @Test
    fun testEnglishInterface() {
        val appArguments = AppArguments(arrayOf("--language", "en"))
        val globalAppExceptionHandler = GlobalAppExceptionHandler()

        runBlocking {
            Ui.boot(appArguments, globalAppExceptionHandler)
        }

        val resourceBundle = ResourceBundle.getBundle("strings/strings", Locale("en"))
        val expectedTitle = resourceBundle.getString("app.title")

        assert(AppInfo.name == expectedTitle) { "Expected: $expectedTitle, but got: ${AppInfo.name}" }
    }
}
