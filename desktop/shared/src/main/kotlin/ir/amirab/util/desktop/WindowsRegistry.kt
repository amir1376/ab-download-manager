package ir.amirab.util.desktop

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

object WindowsRegistry {

    /**
     * Set a REG_SZ value.
     *
     * @param path Full path including root key, e.g.
     * "HKEY_CURRENT_USER\\Software\\MyApp"
     * @param key Value name, or null for the default value.
     */
    fun setValueInRegistry(
        path: String,
        key: String?,
        value: String,
    ) {
        val (root, subKey) = splitPath(path)

        if (!Advapi32Util.registryKeyExists(root, subKey)) {
            Advapi32Util.registryCreateKey(root, subKey)
        }

        Advapi32Util.registrySetStringValue(
            root,
            subKey,
            key ?: "",
            value,
        )
    }

    /**
     * Returns the REG_SZ value, or null if it doesn't exist.
     */
    fun getValueInRegistry(
        path: String,
        key: String?,
    ): String? {
        val (root, subKey) = splitPath(path)

        if (!Advapi32Util.registryValueExists(root, subKey, key ?: "")) {
            return null
        }

        return Advapi32Util.registryGetStringValue(
            root,
            subKey,
            key ?: "",
        )
    }

    /**
     * Deletes the entire registry key.
     */
    fun removePathInRegistry(path: String) {
        val (root, subKey) = splitPath(path)

        if (Advapi32Util.registryKeyExists(root, subKey)) {
            Advapi32Util.registryDeleteKey(root, subKey)
        }
    }

    /**
     * Deletes a value.
     */
    fun removeValueInRegistry(
        path: String,
        key: String?,
    ) {
        val (root, subKey) = splitPath(path)

        if (Advapi32Util.registryValueExists(root, subKey, key ?: "")) {
            Advapi32Util.registryDeleteValue(
                root,
                subKey,
                key ?: "",
            )
        }
    }

    private fun splitPath(path: String): Pair<WinReg.HKEY, String> {
        val normalized = path.replace('/', '\\')

        val index = normalized.indexOf('\\')
        require(index != -1) {
            "Registry path must include root key: $path"
        }

        val rootName = normalized.substring(0, index).uppercase()
        val subKey = normalized.substring(index + 1)

        val root = when (rootName) {
            "HKCU", "HKEY_CURRENT_USER" ->
                WinReg.HKEY_CURRENT_USER

            "HKLM", "HKEY_LOCAL_MACHINE" ->
                WinReg.HKEY_LOCAL_MACHINE

            "HKCR", "HKEY_CLASSES_ROOT" ->
                WinReg.HKEY_CLASSES_ROOT

            "HKU", "HKEY_USERS" ->
                WinReg.HKEY_USERS

            "HKCC", "HKEY_CURRENT_CONFIG" ->
                WinReg.HKEY_CURRENT_CONFIG

            else -> error("Unknown registry root: $rootName")
        }

        return root to subKey
    }
}
