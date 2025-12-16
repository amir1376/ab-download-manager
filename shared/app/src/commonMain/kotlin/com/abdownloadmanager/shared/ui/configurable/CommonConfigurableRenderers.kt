package com.abdownloadmanager.shared.ui.configurable

import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.DayOfWeekConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.FileChecksumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.FloatConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.FolderConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.IntConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.LongConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.NavigatableConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ThemeConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.TimeConfigurable

interface ContainsConfigurableRenderers {
    fun getAllRenderers(): Map<Configurable.Key, ConfigurableRenderer<*>>
}

data class CommonConfigurableRenderers(
    val booleanConfigurableRenderer: ConfigurableRenderer<BooleanConfigurable>,
    val dayOfWeekConfigurableRenderer: ConfigurableRenderer<DayOfWeekConfigurable>,
    val fileChecksumConfigurableRenderer: ConfigurableRenderer<FileChecksumConfigurable>,
    val floatConfigurableRenderer: ConfigurableRenderer<FloatConfigurable>,
    val folderConfigurableRenderer: ConfigurableRenderer<FolderConfigurable>,
    val intConfigurableRenderer: ConfigurableRenderer<IntConfigurable>,
    val longConfigurableRenderer: ConfigurableRenderer<LongConfigurable>,
    val perHostSettingsConfigurableRenderer: ConfigurableRenderer<NavigatableConfigurable>,
    val enumConfigurableRenderer: ConfigurableRenderer<EnumConfigurable<Any>>,
    val speedConfigurableRenderer: ConfigurableRenderer<SpeedLimitConfigurable>,
    val stringConfigurableRenderer: ConfigurableRenderer<StringConfigurable>,
    val themeConfigurableRenderer: ConfigurableRenderer<ThemeConfigurable>,
    val timeConfigurableRenderer: ConfigurableRenderer<TimeConfigurable>,
    val proxyConfigurableRenderer: ConfigurableRenderer<ProxyConfigurable>,

    ) : ContainsConfigurableRenderers {
    override fun getAllRenderers(): Map<Configurable.Key, ConfigurableRenderer<*>> {
        return mapOf(
            BooleanConfigurable.Key to booleanConfigurableRenderer,
            DayOfWeekConfigurable.Key to dayOfWeekConfigurableRenderer,
            FileChecksumConfigurable.Key to fileChecksumConfigurableRenderer,
            FloatConfigurable.Key to floatConfigurableRenderer,
            FolderConfigurable.Key to folderConfigurableRenderer,
            IntConfigurable.Key to intConfigurableRenderer,
            LongConfigurable.Key to longConfigurableRenderer,
            NavigatableConfigurable.Key to perHostSettingsConfigurableRenderer,
            EnumConfigurable.Key to enumConfigurableRenderer,
            SpeedLimitConfigurable.Key to speedConfigurableRenderer,
            StringConfigurable.Key to stringConfigurableRenderer,
            ThemeConfigurable.Key to themeConfigurableRenderer,
            TimeConfigurable.Key to timeConfigurableRenderer,
            ProxyConfigurable.Key to proxyConfigurableRenderer,
        )
    }
}
