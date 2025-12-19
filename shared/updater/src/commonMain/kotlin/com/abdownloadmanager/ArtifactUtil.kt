package com.abdownloadmanager

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class AppArtifactInfo(
    val version: Version,
    val platform: Platform,
    val arch: InstallableArch,
)

sealed interface InstallableArch {
    fun isCompatible(arch: Arch): Boolean
    data object Universal : InstallableArch {
        override fun isCompatible(arch: Arch): Boolean {
            return true
        }
        private val possibleNames = listOf(
            "universal",
            null,
        )

        fun fromString(arch: String?): InstallableArch? {
            return if (arch?.lowercase() in possibleNames) {
                InstallableArch.Universal
            } else {
                null
            }
        }
    }

    data class SomeArch(val arch: Arch) : InstallableArch {
        override fun isCompatible(arch: Arch): Boolean {
            return this.arch == arch
        }

        companion object {
            fun fromString(arch: String?): InstallableArch? {
                return arch
                    ?.let(Arch::fromString)
                    ?.let(::SomeArch)
            }
        }

    }

    companion object {
        fun fromString(archName: String?): InstallableArch? {
            return listOf(
                Universal::fromString,
                SomeArch::fromString,
            ).firstNotNullOf {
                it(archName)
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun InstallableArch.isUniversal(): Boolean {
    contract {
        returns(true) implies (this@isUniversal is InstallableArch.Universal)
    }
    return this is InstallableArch.Universal
}


object ArtifactUtil {
    val artifactRegex =
        "(?<appName>[a-zA-Z]+)_(?<version>(\\d+\\.\\d+\\.\\d+))_(?<platform>[a-zA-Z]+)_(?<arch>[a-zA-Z0-9]+)\\.(?<extension>.+)".toRegex()

    fun getArtifactInfo(name: String): AppArtifactInfo? {
        val values = artifactRegex.find(name)?.groups ?: return null
        val version = runCatching { values.get("version")?.value }
            .getOrNull()
            ?.let(Version::parse)
            ?: return null
        val platform = runCatching { values.get("platform")?.value }
            .getOrNull()
            ?.let(Platform::fromString)
            ?: return null
        val arch = runCatching { values.get("arch")?.value }
            .getOrNull()
            ?.let(InstallableArch::fromString)
            ?: return null
        return AppArtifactInfo(
            version = version,
            platform = platform,
            arch = arch,
        )
    }
}
