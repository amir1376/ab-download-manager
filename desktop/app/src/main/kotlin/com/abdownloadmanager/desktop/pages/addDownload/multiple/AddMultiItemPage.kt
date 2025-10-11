package com.abdownloadmanager.desktop.pages.addDownload.multiple

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.desktop.pages.addDownload.shared.CategoryAddButton
import com.abdownloadmanager.desktop.pages.addDownload.shared.CategorySelect
import com.abdownloadmanager.desktop.pages.addDownload.shared.ExtraConfig
import com.abdownloadmanager.desktop.pages.addDownload.shared.LocationTextField
import com.abdownloadmanager.desktop.pages.addDownload.shared.ShowAddToQueueDialog
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.category.Category
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AddMultiItemPage(
    addMultiDownloadComponent: AddMultiDownloadComponent,
) {
    Column(Modifier) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .weight(1f)
        ) {
            WithContentAlpha(1f) {
                Text(
                    myStringResource(Res.string.add_multi_download_page_header),
                    fontSize = myTextSizes.base
                )
            }
            Spacer(Modifier.height(8.dp))
            AddMultiDownloadTable(
                Modifier.weight(1f),
                addMultiDownloadComponent,
            )
        }
        Footer(
            Modifier,
            addMultiDownloadComponent,
        )
    }
    val currentDownloadConfigurableList by addMultiDownloadComponent.currentDownloadConfigurableList.collectAsState()
    currentDownloadConfigurableList?.let {
        ExtraConfig(
            onDismiss = {
                addMultiDownloadComponent.openConfigurableList(null)
            },
            configurables = it
        )
    }
    if (addMultiDownloadComponent.showAddToQueue) {
        ShowAddToQueueDialog(
            queueList = addMultiDownloadComponent.queueList.collectAsState().value,
            onQueueSelected = { queue, startQueue ->
                addMultiDownloadComponent.requestAddDownloads(
                    queue, startQueue
                )
            },
            onClose = {
                addMultiDownloadComponent.closeAddToQueue()
            }
        )
    }
}

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    component: AddMultiDownloadComponent,
) {
    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaveSettings(
                modifier = Modifier.fillMaxWidth().weight(1f),
                component = component,
            )
            Spacer(Modifier.width(8.dp))
            Row(Modifier.align(Alignment.Bottom)) {
                ActionButton(
                    text = myStringResource(Res.string.add),
                    onClick = {
                        component.openAddToQueueDialog()
                    },
                    enabled = component.canClickAdd,
                    modifier = Modifier,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = myStringResource(Res.string.cancel),
                    onClick = {
                        component.requestClose()
                    },
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
private fun SaveSettings(
    modifier: Modifier,
    component: AddMultiDownloadComponent,
) {
    val selectedCategory by component.selectedCategory.collectAsState()

    val folder by component.folder.collectAsState()

    Column(modifier) {
        Text("${myStringResource(Res.string.save_to)}:")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CategorySaveOption(selectedCategory, component)
            Spacer(Modifier.width(8.dp))
            LocationSaveOption(component, folder)
            Spacer(Modifier)
        }
    }
}

@Composable
private fun RowScope.LocationSaveOption(
    component: AddMultiDownloadComponent,
    folder: String
) {
    val allItemsInSameLocation by component.allInSameLocation.collectAsState()
    SaveOption(
        title = myStringResource(Res.string.all_items_in_one_Location),
        selectedHelp = myStringResource(Res.string.all_items_in_one_Location_description),
        unselectedHelp = myStringResource(Res.string.unselected_all_items_in_specific_location_description),
        selected = allItemsInSameLocation,
        onSelectedChange = {
            component.setAllItemsInSameLocation(it)
        },
        selectedContent = {
            LocationTextField(
                text = folder,
                setText = {
                    component.setFolder(it)
                },
                modifier = Modifier.fillMaxWidth(),
                lastUsedLocations = component.lastUsedLocations.collectAsState().value,
                onRequestRemoveSaveLocation = component::removeFromLastDownloadLocation
            )
        }
    )
}

@Composable
private fun RowScope.CategorySaveOption(
    selectedCategory: Category?,
    component: AddMultiDownloadComponent
) {

    SaveOption(
        title = myStringResource(Res.string.all_items_in_one_category),
        selectedHelp = myStringResource(Res.string.all_items_in_one_category_description),
        unselectedHelp = myStringResource(Res.string.each_item_on_its_own_category_description),
        selected = selectedCategory != null,
        onSelectedChange = {
            if (it) {
                component.setSelectedCategory(component.categories.value.firstOrNull())
            } else {
                component.setSelectedCategory(null)
            }
            component.setAlsoAutoCategorize(!it)
        },
        selectedContent = {
            Row(
                Modifier.height(IntrinsicSize.Max).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategorySelect(
                    categories = component.categories.collectAsState().value,
                    modifier = Modifier.weight(1f),
                    selectedCategory = component.selectedCategory.collectAsState().value,
                    onCategorySelected = {
                        component.setSelectedCategory(it)
                    }
                )
                Spacer(Modifier.width(8.dp))
                CategoryAddButton(
                    Modifier.fillMaxHeight(),
                    enabled = true,
                    onClick = {
                        component.requestAddCategory()
                    },
                )
            }
        }
    )
}

@Composable
private fun RowScope.SaveOption(
    title: String,
    selectedHelp:String,
    unselectedHelp:String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    selectedContent: @Composable () -> Unit
) {
    ExpandableItem(
        modifier=Modifier.fillMaxWidth().weight(1f),
        isExpanded = selected,
        header = {
            Row(
                modifier=Modifier.onClick { onSelectedChange(!selected) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckBox(
                    value = selected,
                    onValueChange = onSelectedChange
                )
                Text(title)
                Help(if (selected) selectedHelp else unselectedHelp)
            }
        },
        body = {
            Column {
                Spacer(Modifier.height(8.dp))
                selectedContent()
            }
        }
    )
}
