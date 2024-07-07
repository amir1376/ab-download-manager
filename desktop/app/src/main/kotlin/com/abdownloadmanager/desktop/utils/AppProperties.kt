package com.abdownloadmanager.desktop.utils

import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.Source
import okio.buffer
import okio.use
import java.util.Properties
import kotlin.io.path.Path

object AppProperties {
    private val defaultProps = Properties()
    private val appProps = Properties(defaultProps)

    private object Paths {
        const val DEFAULT_APP_PROPS_PATH = "configs/app_default.properties"
        const val INSTALLED_APP_PROPS_NAME = "app.properties"
    }

    private object Keys {
        const val CONFIG_DIRECTORY: String = "app.config.path"
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

    private var foundAppProperties = false
    private fun loadAppProps() {
        val resourceDir:String?=System.getProperty("compose.application.resources.dir")
        if (resourceDir.isNullOrBlank()){
            foundAppProperties = false
            return
        }
        val file = Path(resourceDir,Paths.INSTALLED_APP_PROPS_NAME).toOkioPath()
        if (!FileSystem.SYSTEM.exists(file)) {
            // app is in development and don't have app.properties,
            // so we use only default
            foundAppProperties = false
            return
        } else {
            foundAppProperties = true
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

    fun getConfigDirectory(): String {
        return ensureAndGet(Keys.CONFIG_DIRECTORY)
            .toString()
    }
    //app.properties in installation directory
    fun isAppPropertiesFound(): Boolean {
        return foundAppProperties
    }

    val userDir: String get() {
        return System.getProperty("user.home")
    }

    fun boot(){
        loadDefaultProps()
        loadAppProps()
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
