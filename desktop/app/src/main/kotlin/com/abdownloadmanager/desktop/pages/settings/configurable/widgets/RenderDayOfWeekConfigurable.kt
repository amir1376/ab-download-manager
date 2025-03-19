package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.desktop.pages.settings.configurable.DayOfWeekConfigurable
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.asStringSource
import kotlinx.datetime.DayOfWeek
import java.time.DayOfWeek.*

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
        if (!enabled) return
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
                    Modifier.ifThen(!enabled) {
                        alpha(0.5f)
                    }
                ) {
                    allDays.chunked(4).forEach { col ->
                        Column(Modifier.width(IntrinsicSize.Max)) {
                            col.forEach { dayOfWeek ->
                                RenderDayOfWeek(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = enabled,
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
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .ifThen(selected) {
                background(myColors.onBackground / 10)
            }
            .clickable(enabled = enabled) {
                onSelect(dayOfWeek, !selected)
            }
            .padding(vertical = 2.dp)
            .padding(horizontal = 4.dp)

    ) {
        MyIcon(
            MyIcons.check,
            null,
            Modifier.size(8.dp)
                .alpha(if (selected) 1f else 0f),
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = dayOfWeek.asStringSource().rememberString(),
            modifier = Modifier.alpha(
                if (selected) 1f
                else 0.5f
            ),
            fontSize = myTextSizes.xs,
        )
    }
}

private fun DayOfWeek.asStringSource() = when (this) {
    MONDAY -> Res.string.monday
    TUESDAY -> Res.string.tuesday
    WEDNESDAY -> Res.string.wednesday
    THURSDAY -> Res.string.thursday
    FRIDAY -> Res.string.friday
    SATURDAY -> Res.string.saturday
    SUNDAY -> Res.string.sunday
}.asStringSource()