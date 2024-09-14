package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.DayOfWeekConfigurable
import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.abdownloadmanager.desktop.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek

@Composable
fun RenderDayOfWeekConfigurable(cfg: DayOfWeekConfigurable, modifier: Modifier) {
    val value by cfg.stateFlow.collectAsState()
    val setValue = cfg::set
    val allDays = DayOfWeek.values().toSet()
    val enabled = isConfigEnabled()
    fun isSelected(dayOfWeek: DayOfWeek): Boolean {
        return dayOfWeek in value
    }

    fun selectDay(dayOfWeek: DayOfWeek, select: Boolean) {
        if (!enabled)return
        if (select) {
            setValue(
                value.plus(dayOfWeek).sorted().toSet()
            )
        } else {
            setValue(
                value.minus(dayOfWeek).sorted().toSet()
            )
        }
    }
    ConfigTemplate(
        modifier = modifier,
        title = {
            TitleAndDescription(cfg, true)
        },
        value = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.ifThen(!enabled){
                        alpha(0.5f)
                    }
                ) {
                    allDays.chunked(4).forEach { col ->
                        Column(Modifier.width(IntrinsicSize.Max)) {
                            col.forEach { dayOfWeek ->
                                RenderDayOfWeek(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled=enabled,
                                    dayOfWeek = dayOfWeek,
                                    selected = isSelected(dayOfWeek),
                                    onSelect = { s, isSelected ->
                                        selectDay(dayOfWeek, isSelected)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RenderDayOfWeek(
    modifier: Modifier,
    dayOfWeek: DayOfWeek,
    selected: Boolean,
    onSelect: (DayOfWeek, Boolean) -> Unit,
    enabled: Boolean=true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .ifThen(selected) {
                background(myColors.onBackground / 10)
            }
            .clickable(enabled=enabled) {
                onSelect(dayOfWeek, !selected)
            }
            .padding(vertical = 2.dp)
            .padding(horizontal = 4.dp)

    ) {
        MyIcon(
            MyIcons.check,
            null,
            Modifier.size(8.dp)
                .alpha(if (selected)1f else 0f ),
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = dayOfWeek.toString(),
            modifier = Modifier.alpha(
                if(selected) 1f
                else 0.5f
            ),
            fontSize = myTextSizes.xs,
        )
    }
}
