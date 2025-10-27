package com.abdownloadmanager.android.pages.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource


@Composable
fun FilterStatusIndicator(
    component: HomeComponent,
    modifier: Modifier,
    filterMode: HomeComponent.FilterMode.Status,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val filter = component.filterState
    val categoryName = filter.typeCategoryFilter?.name
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        StatusFilterSideButton(
            icon = MyIcons.back,
            modifier = Modifier
                .padding(start = 3.dp)
                .align(Alignment.CenterStart)
        )
        SimplePager(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .fillMaxWidth(),
            pageCount = component.allStatuseFilters.size,
            currentPage = component.currentStatusIndexInList,
            onPageChanged = {
                component.switchToNewStatus(it)
            }
        ) {
            val status = component.allStatuseFilters[it]
            val statusName = status.name.rememberString()
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    statusName,
                    fontSize = myTextSizes.sm,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                categoryName?.let { categoryName ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        categoryName,
                        fontSize = myTextSizes.xs,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        StatusFilterSideButton(
            icon = MyIcons.next,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 3.dp)
        )
        BottomNavigationSelectedIndicator(isSelected)
    }
}

@Composable
fun QueueIndicator(
    modifier: Modifier,
    filterMode: HomeComponent.FilterMode.Queue,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxSize()
    ) {
        Text(
            filterMode.queue.name,
            fontSize = myTextSizes.sm,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        BottomNavigationSelectedIndicator(isSelected)
    }
}

@Composable
private fun StatusFilterSideButton(
    icon: IconSource,
    modifier: Modifier,
) {
    MyIcon(
        icon = icon,
        contentDescription = null,
        modifier = modifier
            .size(12.dp)
            .alpha(0.55f),
    )
}
