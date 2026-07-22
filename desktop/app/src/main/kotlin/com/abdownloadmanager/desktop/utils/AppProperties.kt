package com.abdownloadmanager.desktop.utils

import okio.*
import okio.Path.Companion.toPath
import java.util.*

object AppProperties {
    private val defaultProps = Properties()
    private val appProps = Properties(defaultProps)

    private object Paths {
        const val DEFAULT_APP_PROPS_PATH = "configs/app_default.properties"
    }

    private object Keys {
        const val DEBUG: String = "app.debug"
    }

    private fun loadFromSource(props: Properties, source: Source) {
        source.buffer()
            .inputStream()
            .use {
                props.load(it)
            }
    }

    private fun loadDefaultProps() {
        FileSystem.RESOURCES
            .source(Paths.DEFAULT_APP_PROPS_PATH.toPath())
            .use {
                loadFromSource(defaultProps, it)
            }
    }

    private fun loadAppProps(file: Path) {
        if (!FileSystem.SYSTEM.exists(file)) {
            return
        }
        FileSystem.SYSTEM
            .source(file)
            .use {
                loadFromSource(appProps, it)
            }
    }

    private fun ensureAndGet(key: String): Any {
        return requireNotNull(
            appProps.getProperty(key)
        ) { "key: '$key' not found in properties file" }
            .hydrateVariables()
            .trim('"')
            .trim('\'')
    }

    fun isDebugMode(): Boolean {
        return ensureAndGet(Keys.DEBUG)
            .toString()
            .toBoolean()
    }

    val userDir: String
        get() {
            return System.getProperty("user.home")
        }

    fun boot(appPropertiesFile: Path) {
        loadDefaultProps()
        loadAppProps(appPropertiesFile)
    }

    fun getAll() = appProps
}

private val variableRegex = "\\$\\{(.*?)\\}".toRegex()
private fun String.hydrateVariables(): String {
    return variableRegex.replace(this) {
        val variableName = it.groupValues[1]
        System.getProperty(variableName)
    }
}
