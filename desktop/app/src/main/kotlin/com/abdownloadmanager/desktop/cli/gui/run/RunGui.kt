package com.abdownloadmanager.desktop.cli.gui.run

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.MainApplication
import com.abdownloadmanager.desktop.utils.AppInfo
import com.abdownloadmanager.desktop.utils.createAndSetGlobalExceptionHandler
import com.abdownloadmanager.desktop.utils.isInIDE
import com.abdownloadmanager.desktop.utils.singleInstance.AnotherInstanceIsRunning
import com.abdownloadmanager.desktop.utils.singleInstance.SingleInstanceManager
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.nucleusframework.aot.runtime.AotRuntime
import ir.amirab.util.logger.appLogger
import ir.amirab.util.writeText
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class RunGui : SuspendingCliktCommand(AppArguments.Commands.RUN) {
    val background by option(AppArguments.Args.BACKGROUND).flag()

    override suspend fun run() {
        AppArguments.update {
            it.copy(startSilent = background)
        }
        try {
            trainTheAOT()
            defaultApp(AppArguments.get())
        } catch (e: Throwable) {
            appLogger.e(throwable = e) { "Fail to start the ${AppInfo.displayName} app because:" }
            echo(
                message = ("Fail to start the ${AppInfo.displayName} app because:"),
                err = true
            )
            e.printStackTrace()
            AppInfo.definedPaths.crashLogFile.writeText(e.stackTraceToString())
            throw Abort()
        }
    }

    private fun defaultApp(
        appArguments: AppArguments,
    ) {
        val singleInstance = SingleInstanceManager.get()
        try {
            singleInstance.lockInstance()
        } catch (_: AnotherInstanceIsRunning) {
            echo("instance already running")
            runBlocking {
                singleInstance.singleInstanceService().useService {
                    it.showUserThatAppIsRunning()
                }
            }
            return
        }
        val startedMessage = "${AppInfo.displayName}-${AppInfo.version} started"
        appLogger.i { startedMessage }
        echo(startedMessage)
        if (AppInfo.isInIDE()) {
            appLogger.i { "App is in IDE" }
        }

        val globalExceptionHandler = createAndSetGlobalExceptionHandler()
        MainApplication().use {
            it.start(
                appArguments = appArguments,
                globalAppExceptionHandler = globalExceptionHandler,
            )
        }
    }


    private fun trainTheAOT() {
        if (!AotRuntime.isTraining()) {
            return
        }
        thread {
            Thread.sleep(30_000)
            throw ProgramResult(0)
        }
    }
}
