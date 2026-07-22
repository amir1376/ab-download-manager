package com.abdownloadmanager.desktop


data class AppArguments(
    val debug: Boolean = false,
    val startSilent: Boolean = false,
) {
    companion object {
        private var instance: AppArguments = AppArguments()

        fun get() = instance

        /**
         * Initial me on app startup
         */

        fun update(update: (AppArguments) -> AppArguments) {
            synchronized(instance) {
                instance = update(instance)
            }
        }
    }

    object Commands {
        const val GUI = "gui"
        const val RUN = "run"
        const val EXIT = "exit"
        const val START_IF_NOT_STARTED = "start-if-not-started"
    }
    object Args {
        const val BACKGROUND = "--background"
        const val DEBUG = "--debug"
    }
}
