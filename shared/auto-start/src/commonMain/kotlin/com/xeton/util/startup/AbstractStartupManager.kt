package com.xeton.util.startup

abstract class AbstractStartupManager {
    @Throws(Exception::class)
    abstract fun install()
    abstract fun uninstall()
}
