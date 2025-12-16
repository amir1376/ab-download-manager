package ir.amirab.util.osfileutil

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.*
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import ir.amirab.util.execAndWait
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolute

internal class WindowsFileUtils : DesktopFileUtils() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "start", "/B", "", file.path.quoted()))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        val nativeSuccess = showFileInFolderViaNative(file.path)
        if (nativeSuccess) {
            return true
        }
        //fallback to use explorer
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", "/select,", file.path.quoted()))
    }

    override fun openFolderInternal(folder: File): Boolean {
        val nativeSuccess = openFolderViaNative(folder.path)
        if (nativeSuccess) {
            return true
        }
        //fallback to use explorer
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", folder.path.quoted()))
    }

    override fun isRemovableStorage(path: String): Boolean {
        return try {
            isRemovableStorageViaNative(path)
        } catch (e: Exception) {
            e.printStackTrace()
            super.isRemovableStorage(path)
        }
    }

    private fun isRemovableStorageViaNative(path: String): Boolean {
        val rootPath = Path(path).absolute().root.toString()
        val driveType = Kernel32.INSTANCE.GetDriveType(rootPath)
        return driveType == WinBase.DRIVE_REMOVABLE
    }

    private fun showFileInFolderViaNative(
        file: String,
    ): Boolean {
        try {
            Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED)
            val path = Shell32Ex.INSTANCE.ILCreateFromPath(File(file).parent)
            val selectedFiles = arrayOf(Shell32Ex.INSTANCE.ILCreateFromPath(file))
            val cidl = WinDef.UINT(selectedFiles.size.toLong())
            try {
                val res = Shell32Ex.INSTANCE.SHOpenFolderAndSelectItems(
                    pIdlFolder = path,
                    cIdl = cidl,
                    apIdl = selectedFiles,
                    dwFlags = WinDef.DWORD(0)
                )
                return WinError.S_OK == res
            } finally {
                Shell32Ex.INSTANCE.ILFree(path)
                selectedFiles.forEach {
                    Shell32Ex.INSTANCE.ILFree(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            Ole32.INSTANCE.CoUninitialize()
        }
    }

    private fun openFolderViaNative(folder: String): Boolean {
        try {
            val result = Shell32.INSTANCE.ShellExecute(
                null, "explore", folder, null, null, WinUser.SW_NORMAL,
            ).toInt()
            return result > 32
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun String.quoted() = "\"$this\""

}


private interface Shell32Ex : StdCallLibrary {
    fun ILCreateFromPath(path: String?): Pointer?
    fun ILFree(pIdl: Pointer?)
    fun SHOpenFolderAndSelectItems(
        pIdlFolder: Pointer?,
        cIdl: WinDef.UINT?,
        apIdl: Array<Pointer?>?,
        dwFlags: WinDef.DWORD?,
    ): WinNT.HRESULT?

    companion object {
        val INSTANCE: Shell32Ex = Native.load("shell32", Shell32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}
