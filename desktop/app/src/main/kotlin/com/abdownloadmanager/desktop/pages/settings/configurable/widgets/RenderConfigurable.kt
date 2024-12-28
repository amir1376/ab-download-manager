package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
@Immutable
data class ConfigGroupInfo(
    val visible:Boolean,
    val enabled:Boolean,
)

@Suppress("UNCHECKED_CAST")
@Composable
fun RenderConfigurable(
    cfg: Configurable<*>,
    modifier: Modifier,
    groupInfo: ConfigGroupInfo?=null,
) {
    ConfigurationWrapper(
        configurable = cfg,
        groupInfo=groupInfo
    ){
        when(cfg){
            is BooleanConfigurable -> RenderBooleanConfig(cfg,modifier)
            is FloatConfigurable ->RenderFloatConfig(cfg,modifier)
            is IntConfigurable -> RenderIntegerConfig(cfg,modifier)
            is BaseLongConfigurable -> {
                when(cfg){
                    is LongConfigurable -> RenderLongConfig(cfg,modifier)
                    is SpeedLimitConfigurable -> RenderSpeedConfig(cfg,modifier)
                }
            }
            is BaseEnumConfigurable -> {
                when (cfg) {
                    is ThemeConfigurable -> RenderThemeConfig(cfg, modifier)
                    is EnumConfigurable -> RenderEnumConfig(cfg as EnumConfigurable<Any?>, modifier)
                }
            }
            is StringConfigurable ->{
                when (cfg) {
                    is FolderConfigurable -> RenderFolderConfig(cfg,modifier)
                    else -> RenderStringConfig(cfg,modifier)
                }
            }
            is TimeConfigurable -> {
                RenderTimeConfig(cfg,modifier)
            }

            is DayOfWeekConfigurable -> RenderDayOfWeekConfigurable(cfg,modifier)
            is ProxyConfigurable -> RenderProxyConfig(cfg, modifier)
        }
    }
}

