package ir.amirab.installer

import ir.amirab.util.platform.Platform


enum class InstallerTargetFormat(
    val id: String,
    val targetOS: Platform,
) {
    Deb("deb", Platform.Desktop.Linux),
    Rpm("rpm", Platform.Desktop.Linux),
    Dmg("dmg", Platform.Desktop.MacOS),
    Pkg("pkg", Platform.Desktop.MacOS),
    Exe("exe", Platform.Desktop.Windows),
    Msi("msi", Platform.Desktop.Windows),
    Apk("apk", Platform.Android);

    val isCompatibleWithCurrentOS: Boolean by lazy { isCompatibleWith(Platform.getCurrentPlatform()) }

    fun isCompatibleWith(os: Platform): Boolean = os == targetOS

    val outputDirName: String
        get() = id

    val fileExt: String
        get() {
            return ".$id"
        }
}
