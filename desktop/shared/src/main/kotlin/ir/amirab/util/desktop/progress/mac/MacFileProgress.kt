package ir.amirab.util.desktop.progress.mac

import com.sun.jna.Memory
import com.sun.jna.Pointer
import ir.amirab.util.desktop.progress.FileProgressHandle
import ir.amirab.util.desktop.progress.PlatformFileProgress
import ir.amirab.util.desktop.utils.mac.FoundationLibrary

/**
 * Publishes download progress as an NSProgress so Finder draws its normal
 * download pie on the file icon.
 */
class MacFileProgress : PlatformFileProgress {
    private val foundation = FoundationLibrary.INSTANCE
    private val isAvailable = foundation != null

    private val nsStringClass by lazy { foundation!!.objc_getClass("NSString") }
    private val nsUrlClass by lazy { foundation!!.objc_getClass("NSURL") }
    private val nsProgressClass by lazy { foundation!!.objc_getClass("NSProgress") }

    private val stringWithUTF8StringSel by lazy { foundation!!.sel_registerName("stringWithUTF8String:") }
    private val fileURLWithPathSel by lazy { foundation!!.sel_registerName("fileURLWithPath:") }
    private val allocSel by lazy { foundation!!.sel_registerName("alloc") }
    private val initWithParentSel by lazy { foundation!!.sel_registerName("initWithParent:userInfo:") }
    private val setKindSel by lazy { foundation!!.sel_registerName("setKind:") }
    private val setUserInfoObjectSel by lazy { foundation!!.sel_registerName("setUserInfoObject:forKey:") }
    private val publishSel by lazy { foundation!!.sel_registerName("publish") }

    override fun start(path: String): FileProgressHandle? {
        if (!isAvailable) return null
        return runCatching { createProgress(path) }.getOrNull()
    }

    private fun createProgress(path: String): FileProgressHandle {
        val f = foundation!!
        val pool = f.objc_autoreleasePoolPush()
        try {
            val progress = f.objc_msgSend(nsProgressClass, allocSel)
            val initialized = f.objc_msgSend(progress, initWithParentSel, Pointer(0), Pointer(0))

            val fileUrl = f.objc_msgSend(
                nsUrlClass,
                fileURLWithPathSel,
                createNSString(path),
            )
            f.objc_msgSend(initialized, setKindSel, createNSString("NSProgressKindFile"))
            f.objc_msgSend(
                initialized,
                setUserInfoObjectSel,
                fileUrl,
                createNSString("NSProgressFileURLKey"),
            )
            f.objc_msgSend(
                initialized,
                setUserInfoObjectSel,
                createNSString("NSProgressFileOperationKindDownloading"),
                createNSString("NSProgressFileOperationKindKey"),
            )
            f.objc_msgSend(initialized, publishSel)
            return MacFileProgressHandle(f, initialized)
        } finally {
            f.objc_autoreleasePoolPop(pool)
        }
    }

    private fun createNSString(value: String): Pointer {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val buffer = Memory(bytes.size + 1L)
        buffer.write(0, bytes, 0, bytes.size)
        buffer.setByte(bytes.size.toLong(), 0)
        return foundation!!.objc_msgSend(nsStringClass, stringWithUTF8StringSel, buffer)
    }
}

private class MacFileProgressHandle(
    private val foundation: FoundationLibrary,
    private val progress: Pointer,
) : FileProgressHandle {
    private val totalUnitCountSel by lazy { foundation.sel_registerName("setTotalUnitCount:") }
    private val completedUnitCountSel by lazy { foundation.sel_registerName("setCompletedUnitCount:") }
    private val unpublishSel by lazy { foundation.sel_registerName("unpublish") }

    private var ended = false

    override fun update(completed: Long, total: Long) {
        if (ended) return
        foundation.objc_msgSend(progress, totalUnitCountSel, total)
        foundation.objc_msgSend(progress, completedUnitCountSel, completed)
    }

    override fun end() {
        if (ended) return
        ended = true
        foundation.objc_msgSend(progress, unpublishSel)
        foundation.objc_release(progress)
    }
}
