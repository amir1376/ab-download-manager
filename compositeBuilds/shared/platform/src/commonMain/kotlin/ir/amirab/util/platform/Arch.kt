package ir.amirab.util.platform

sealed class Arch(val name: String) {
    data object X64 : Arch("x64")
    data object Arm64 : Arch("arm64")
    data object X32 : Arch("x32")
    data object Arm32 : Arch("arm32")

    override fun toString(): String {
        return name
    }

    companion object : ArchFinder by JvmArchFinder() {
        private val DefinedArchStrings = mapOf(
            X64 to listOf(
                "amd64", "x64", "x86_64"
            ),
            Arm64 to listOf(
                "arm64", "aarch64"
            ),
            X32 to listOf(
                "x86", "i686"
            ),
            Arm32 to listOf(
                "armv8l", "armv7l", "arm"
            ),
        )

        fun fromString(archName: String): Arch? {
            val a = archName.lowercase()
            return DefinedArchStrings.entries.firstOrNull {
                a in it.value
            }?.key
        }
    }
}

interface ArchFinder {
    fun getCurrentArch(): Arch
}

private class JvmArchFinder : ArchFinder {
    private val _arch by lazy {
        getCurrentArchFromJVMProperty()
    }

    private fun getCurrentArchFromJVMProperty(): Arch {
        val osString = System.getProperty("os.arch").lowercase()
        return requireNotNull(Arch.fromString(osString)) {
            "this arch is not recognized: $osString"
        }
    }

    override fun getCurrentArch(): Arch {
        return _arch
    }
}
