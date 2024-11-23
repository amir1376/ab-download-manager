package com.abdownloadmanager.desktop.pages.addDownload.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.*
import com.abdownloadmanager.desktop.pages.category.toCategoryImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Plus
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.utils.category.Category
import com.abdownloadmanager.utils.compose.widget.Icon

@Composable
fun CategorySelect(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
) {
    var isSelectionOpen by remember {
        mutableStateOf(false)
    }
    val closeDialog = {
        isSelectionOpen = false
    }
    DialogDropDown(
        selectedItem = selectedCategory,
        possibleItems = categories,
        onItemSelected = onCategorySelected,
        enabled = enabled,
        renderItem = {
            RenderCategory(
                category = it,
                modifier = Modifier,
            )
        },
        dropdownOpen = isSelectionOpen,
        onRequestCloseDropDown = {
            closeDialog()
        },
        onRequestOpenDropDown = {
            isSelectionOpen = true
        },
        modifier = modifier,
        dropDownSize = DpSize(220.dp, 220.dp),
    )
}

@Composable
private fun RenderCategory(
    modifier: Modifier,
    category: Category,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = category.toCategoryImageVector()
        val iconModifier = Modifier.size(16.dp)
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = iconModifier,
            )
        } else {
            Spacer(iconModifier)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            category.name,
            softWrap = false,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CategoryAddButton(
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val borderColor = myColors.onBackground / 0.1f
    val background = myColors.surface / 50
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .clip(shape)
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .border(1.dp, borderColor, shape)
            .background(background)
            .clickable(
                enabled = enabled
            ) { onClick() }
            .aspectRatio(1f)
//            .padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = AbIcons.Default.Plus,
            contentDescription = "Add Category",
            modifier = Modifier
                .align(Alignment.Center)
                .size(16.dp)
        )
    }
}