package ir.amirab.util.osfileutil

import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.fileStore

abstract class DesktopFileUtils : FileUtilsBase() {
    override fun isRemovableStorage(path: String): Boolean {
        runCatching {
            val store = Path(path).absolute().fileStore()
            if (store.supportsFileAttributeView("basic")) {
                val isRemovable = store.getAttribute("volume:isRemovable")
                if (isRemovable is Boolean) {
                    return isRemovable
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
        return false
    }
}
