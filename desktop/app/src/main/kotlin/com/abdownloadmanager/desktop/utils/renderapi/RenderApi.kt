package com.abdownloadmanager.desktop.utils.renderapi

import com.abdownloadmanager.shared.util.BaseStorage
import ir.amirab.util.createParentDirectories
import ir.amirab.util.deleteIfExists
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform
import ir.amirab.util.readText
import ir.amirab.util.writeText
import kotlinx.coroutines.flow.MutableStateFlow
import okio.Path

/**
 * this class provides these features
 * - override the default render api on some platforms
 * - save/restore user selected render api
 * ### Note
 * [boot] must be called before any compose UI logic.
 * @param file the file path the to save the renderApi backend
 */
class CustomRenderApi(
    private val file: Path
) : BaseStorage<RenderApi?>() {
    fun boot() {
        startPersistData()
        val customRenderApiRequested =
            System.getenv("SKIKO_RENDER_API") != null ||
                    System.getProperty("skiko.renderApi") != null
        if (!customRenderApiRequested) {
            val renderApi = getSavedValueIfSupported()
                ?: getOurDefaultRenderApi()
            if (renderApi != null) {
                System.setProperty("skiko.renderApi", renderApi.name)
            }
        }
    }


    private fun getSavedValueIfSupported(): RenderApi? {
        return get()?.takeIf {
            isRenderApiSupportedInThisPlatform(it)
        }
    }

    private fun get(): RenderApi? {
        return this.data.value
    }

    private fun getOurDefaultRenderApi(): RenderApi? {
        return when (Platform.getCurrentPlatform()) {
            Platform.Desktop.Linux -> RenderApi.SOFTWARE
            Platform.Desktop.Windows -> {
                val arch = Arch.getCurrentArch()
                when (arch) {
                    Arch.X64 -> RenderApi.OPENGL
                    Arch.Arm64 -> RenderApi.ANGLE
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun isRenderApiSupportedInThisPlatform(renderApi: RenderApi): Boolean {
        return getSupportedRenderApiForThisPlatform().contains(renderApi)
    }

    fun getSupportedRenderApiForThisPlatform(): List<RenderApi> {
        return supportedRenderApisPerPlatform.getOrDefault(Platform.getCurrentPlatform(), emptyList())
    }

    private val supportedRenderApisPerPlatform = mapOf(
        Platform.Desktop.Windows to listOf(
            RenderApi.DIRECT3D,
            RenderApi.OPENGL,
            RenderApi.ANGLE,
            RenderApi.SOFTWARE,
        ),
        Platform.Desktop.Linux to listOf(
            RenderApi.OPENGL,
            RenderApi.SOFTWARE,
        ),
        Platform.Desktop.MacOS to listOf(
            RenderApi.METAL,
            RenderApi.OPENGL,
            RenderApi.SOFTWARE,
        ),
    )

    override val inMemoryState: MutableStateFlow<RenderApi?> = MutableStateFlow(
        runCatching {
            RenderApi.valueOf(file.readText())
        }.getOrNull()
    )

    override suspend fun saveData(data: RenderApi?) {
        runCatching {
            if (data != null) {
                file.createParentDirectories()
                file.writeText(data.name)
            } else {
                file.deleteIfExists()
            }
        }
    }
}

enum class RenderApi(val prettyName: String) {
    SOFTWARE("Software"),
    DIRECT3D("DirectX"),
    METAL("Metal"),
    OPENGL("Open GL"),
    ANGLE("ANGLE"),
}
