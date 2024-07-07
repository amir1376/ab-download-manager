package com.abdownloadmanager.desktop.utils

import kotlin.io.path.Path
import kotlin.io.path.name

object FileNameValidator{
    fun isValidFileName(name: String): Boolean {
        if (name.isEmpty())return false
        return runCatching {
            Path(name)
        }.getOrNull()?.let {
            it.name==name
        }?:false
    }
}
