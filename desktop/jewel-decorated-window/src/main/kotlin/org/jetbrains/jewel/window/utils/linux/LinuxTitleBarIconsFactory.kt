package org.jetbrains.jewel.window.utils.linux

import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.styling.TitleBarIcons

public object LinuxTitleBarIconsFactory {
    /**
     * Crée les icônes de barre de titre appropriées pour l'environnement de
     * bureau Linux détecté.
     */
    fun createForCurrentDesktop(): TitleBarIcons =
        when (LinuxDesktopEnvironment.Current) {
            LinuxDesktopEnvironment.GNOME -> createGnomeIcons()
            LinuxDesktopEnvironment.KDE -> createKdeIcons()
            LinuxDesktopEnvironment.UNKNOWN -> createGnomeIcons() // Fallback vers Gnome
        }

    /** Crée les icônes pour l'environnement GNOME. */
    fun createGnomeIcons(): TitleBarIcons =
        TitleBarIcons(
            minimizeButton = AllIconsKeys.Linux.Theme.Gnome.Minimize,
            maximizeButton = AllIconsKeys.Linux.Theme.Gnome.Maximize,
            restoreButton = AllIconsKeys.Linux.Theme.Gnome.Restore,
            closeButton = AllIconsKeys.Linux.Theme.Gnome.Close,
        )

    /** Crée les icônes pour l'environnement KDE. */
    fun createKdeIcons(): TitleBarIcons =
        TitleBarIcons(
            minimizeButton = AllIconsKeys.Linux.Theme.Kde.Minimize,
            maximizeButton = AllIconsKeys.Linux.Theme.Kde.Maximize,
            restoreButton = AllIconsKeys.Linux.Theme.Kde.Restore,
            closeButton = AllIconsKeys.Linux.Theme.Kde.Close,
        )
}
