package com.abdownloadmanager.desktop.ui.configurable.comon

import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.BooleanConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.DayOfWeekConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.EnumConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.FileChecksumConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.FloatConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.FolderConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.IntConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.LongConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.PerHostSettingsConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.SpeedLimitConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.StringConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.ThemeConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.TimeConfigurableRenderer
import com.abdownloadmanager.desktop.ui.configurable.comon.renderer.ProxyConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.CommonConfigurableRenderers

val CommonConfigurableRenderersForDesktop = CommonConfigurableRenderers(
    booleanConfigurableRenderer = BooleanConfigurableRenderer,
    dayOfWeekConfigurableRenderer = DayOfWeekConfigurableRenderer,
    fileChecksumConfigurableRenderer = FileChecksumConfigurableRenderer,
    floatConfigurableRenderer = FloatConfigurableRenderer,
    folderConfigurableRenderer = FolderConfigurableRenderer,
    intConfigurableRenderer = IntConfigurableRenderer,
    longConfigurableRenderer = LongConfigurableRenderer,
    perHostSettingsConfigurableRenderer = PerHostSettingsConfigurableRenderer,
    enumConfigurableRenderer = EnumConfigurableRenderer,
    speedConfigurableRenderer = SpeedLimitConfigurableRenderer,
    stringConfigurableRenderer = StringConfigurableRenderer,
    themeConfigurableRenderer = ThemeConfigurableRenderer,
    timeConfigurableRenderer = TimeConfigurableRenderer,
    proxyConfigurableRenderer = ProxyConfigurableRenderer,
)
