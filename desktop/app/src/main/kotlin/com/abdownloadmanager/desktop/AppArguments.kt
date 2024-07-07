package com.abdownloadmanager.desktop

data class AppArguments(
    val getIntegrationPort: Boolean,
    val startIfNotStarted: Boolean,
    val startSilent: Boolean,
    val debug: Boolean,
) {
    companion object {
        private lateinit var instance: AppArguments
        fun get() = instance

        /**
         * Initial me on app startup
         */
        fun init(args: Array<String>) {
            instance = create(args)
        }

        private fun create(args: Array<String>): AppArguments {
            return AppArguments(
                getIntegrationPort = args.contains(Args.GET_INTEGRATION_PORT),
                startIfNotStarted = args.contains(Args.START_IF_NOT_STARTED),
                startSilent = args.contains(Args.BACKGROUND),
                debug = args.contains(Args.DEBUG),
            )
        }
    }

    object Args {
        const val START_IF_NOT_STARTED = "--start-if-not-started"
        const val BACKGROUND = "--background"
        const val GET_INTEGRATION_PORT = "--get-integration-port"
        const val DEBUG = "--debug"
    }
}