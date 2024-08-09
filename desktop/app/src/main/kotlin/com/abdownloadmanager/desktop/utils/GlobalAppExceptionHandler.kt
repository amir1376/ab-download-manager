package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.ui.error.ErrorWindow
import com.abdownloadmanager.desktop.ui.theme.ABDownloaderTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import com.abdownloadmanager.desktop.pages.settings.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.awt.Window
import java.awt.event.WindowEvent
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.exitProcess

interface GlobalAppExceptionHandler : UncaughtExceptionHandler,
    WindowExceptionHandlerFactory {

    @Composable
    fun content()

    /**
     * tell handler to exit app after crash, this app is useless
     */
    fun onProcessIsUseless()
}

private class GlobalExceptionHandlerImpl : GlobalAppExceptionHandler {
    private val isRenderedInAppScope = MutableStateFlow(false)

    private var exitProcessOnClose = AtomicBoolean(false)
    override fun onProcessIsUseless() {
        exitProcessOnClose.set(true)
    }

    private fun shouldExitAppInsteadOfClose(): Boolean {
        return exitProcessOnClose.get()
    }

    private fun showErrorInUi(throwable: Throwable) {
        if (isRenderedInAppScope.value) {
            showErrorInCurrentApplicationScope(throwable)
        } else {
            showErrorInNewApplicationScope(throwable)
        }
    }

    val activeThrowableList = MutableStateFlow(emptyList<Throwable>())
    private fun showErrorInCurrentApplicationScope(throwable: Throwable) {
        activeThrowableList.update {
            it + throwable
        }
    }

    private fun showErrorInNewApplicationScope(throwable: Throwable) {
        kotlin.runCatching {
            application(exitProcessOnExit = false) {
                val close = {
                    if (shouldExitAppInsteadOfClose()) {
                        exitProcess(0)
                    } else {
                        exitApplication()
                    }
                }
                ABDownloaderTheme(
                    ThemeManager.DefaultTheme,
                ) {
                    ErrorWindow(throwable, close)
                }
            }
        }.onFailure {
            println("We have error in Global Exception Handler")
            it.printStackTrace()
        }
    }


    @Composable
    override fun content() {
        DisposableEffect(Unit) {
            isRenderedInAppScope.update { true }
            onDispose {
                isRenderedInAppScope.update { false }
            }
        }
        val list = activeThrowableList.collectAsState().value
        for (throwable in list) {
            ErrorWindow(
                throwable = throwable,
                close = {
                    activeThrowableList.update {
                        it - throwable
                    }
                    if (activeThrowableList.value.isEmpty()) {
                        if (shouldExitAppInsteadOfClose()) {
                            exitProcess(0)
                        }
                    }
                }
            )
        }
    }

    private fun showErrorInConsole(thread: Thread, e: Throwable) {
        val output = System.err
        output.println("""Exception in thread "${thread.name}" ${e::class.qualifiedName}""")
        e.printStackTrace(output)
    }

    private fun showErrorInConsole(window: Window, throwable: Throwable) {
        val output = System.err
        output.println("""Exception in windows $window ,${throwable::class.qualifiedName}""")
        throwable.printStackTrace(output)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        showErrorInConsole(t, e)
        showErrorInUi(e)
    }

    override fun exceptionHandler(window: Window): WindowExceptionHandler {
        return WindowExceptionHandler {
            thread {
                showErrorInConsole(window, it)
                showErrorInUi(it)
                //await exit
                window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            }
        }
    }
}

@Composable
fun ProvideGlobalExceptionHandler(
    eh: GlobalAppExceptionHandler,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalWindowExceptionHandlerFactory provides eh) {
        content()
        eh.content()
    }
}

fun createAndSetGlobalExceptionHandler(): GlobalAppExceptionHandler {
    val handler = GlobalExceptionHandlerImpl()
    Thread.setDefaultUncaughtExceptionHandler(handler)
    return handler
}