package com.abdownloadmanager.android.pages.home.sections.sort

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.home.HomeComponent
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.sort.Sort
import com.abdownloadmanager.shared.ui.widget.sort.SortIndicatorMode
import com.abdownloadmanager.shared.ui.widget.sort.isDescending
import com.abdownloadmanager.shared.ui.widget.sort.toSortIndicatorMode
import com.abdownloadmanager.shared.ui.widget.sort.next
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.modifiers.hijackClick
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen

@Composable
fun RenderSortMenu(
    component: HomeComponent,
    modifier: Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
) {
    val isShowingSortMenu by component.isSortMenuShowing.collectAsState()
    AnimatedVisibility(
        modifier = modifier,
        visible = isShowingSortMenu,
        enter = enter,
        exit = exit,
    ) {
        BackHandler {
            component.setIsSortMenuShowing(false)
        }
        val shape = myShapes.defaultRounded
        Column(
            Modifier
                .clip(shape)
                .hijackClick()
                .background(myColors.surface, shape)
                .border(1.dp, myColors.onSurface / 0.2f, shape)
        ) {
            val selectedSort by component.selectedSort.collectAsState()
            Text(
                text = myStringResource(Res.string.sort_by),
                fontWeight = FontWeight.Bold,
                fontSize = myTextSizes.xl,
                modifier = Modifier.padding(
                    mySpacings.largeSpace
                )
            )
            for (downloadSortBy in component.possibleSorts) {
                val isSelected = downloadSortBy == selectedSort.cell
                key(downloadSortBy) {
                    SortItem(
                        downloadSortBy,
                        sortIndicatorMode = if (isSelected) {
                            selectedSort.toSortIndicatorMode()
                        } else {
                            SortIndicatorMode.None
                        },
                        onSortChange = {
                            component.setSelectedSort(
                                Sort(
                                    cell = downloadSortBy,
                                    isDescending = it.isDescending()
                                )
                            )
                        },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
            ActionButton(
                text = myStringResource(Res.string.ok),
                onClick = {
                    component.setIsSortMenuShowing(false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        mySpacings.largeSpace
                    ),
            )
        }
    }
}

@Composable
private fun SortItem(
    sortBy: DownloadSortBy,
    sortIndicatorMode: SortIndicatorMode,
    onSortChange: (SortIndicatorMode) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onSortChange(sortIndicatorMode.next())
            }
            .ifThen(sortIndicatorMode == SortIndicatorMode.None) {
                alpha(0.6f)
            }
            .heightIn(min = mySpacings.thumbSize)
            .padding(horizontal = mySpacings.largeSpace),
    ) {
        MyIcon(sortBy.icon, null, Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            sortBy.name.rememberString(),
            Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        RenderSortIndicatorMode(sortIndicatorMode)
    }
}

@Composable
fun RenderSortIndicatorMode(sortIndicatorMode: SortIndicatorMode) {
    val icon = when (sortIndicatorMode) {
        SortIndicatorMode.None -> null
        SortIndicatorMode.Ascending -> MyIcons.sortUp
        SortIndicatorMode.Descending -> MyIcons.sortDown
    }
    icon?.let {
        MyIcon(
            it,
            null,
            Modifier
                .size(16.dp)
                .alpha(0.75f)
        )
    }
}
