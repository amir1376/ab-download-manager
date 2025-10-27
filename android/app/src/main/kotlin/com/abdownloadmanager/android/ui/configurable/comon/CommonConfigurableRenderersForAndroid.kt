package com.abdownloadmanager.android.ui.configurable.comon

import com.abdownloadmanager.android.ui.configurable.comon.renderer.BooleanConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.DayOfWeekConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.EnumConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.FileChecksumConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.FloatConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.FolderConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.IntConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.LongConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.NavigatableConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.ProxyConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.SpeedLimitConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.StringConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.ThemeConfigurableRenderer
import com.abdownloadmanager.android.ui.configurable.comon.renderer.TimeConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.CommonConfigurableRenderers

val CommonConfigurableRenderersForAndroid = CommonConfigurableRenderers(
    booleanConfigurableRenderer = BooleanConfigurableRenderer,
    dayOfWeekConfigurableRenderer = DayOfWeekConfigurableRenderer,
    fileChecksumConfigurableRenderer = FileChecksumConfigurableRenderer,
    floatConfigurableRenderer = FloatConfigurableRenderer,
    folderConfigurableRenderer = FolderConfigurableRenderer,
    intConfigurableRenderer = IntConfigurableRenderer,
    longConfigurableRenderer = LongConfigurableRenderer,
    perHostSettingsConfigurableRenderer = NavigatableConfigurableRenderer,
    enumConfigurableRenderer = EnumConfigurableRenderer,
    speedConfigurableRenderer = SpeedLimitConfigurableRenderer,
    stringConfigurableRenderer = StringConfigurableRenderer,
    themeConfigurableRenderer = ThemeConfigurableRenderer,
    timeConfigurableRenderer = TimeConfigurableRenderer,
    proxyConfigurableRenderer = ProxyConfigurableRenderer,
)
