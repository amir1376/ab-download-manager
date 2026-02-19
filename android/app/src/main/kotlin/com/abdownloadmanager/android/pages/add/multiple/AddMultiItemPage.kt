package com.abdownloadmanager.android.pages.add.multiple

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.add.shared.CategoryAddButton
import com.abdownloadmanager.android.pages.add.shared.CategorySelect
import com.abdownloadmanager.android.pages.add.shared.ExtraConfig
import com.abdownloadmanager.android.pages.add.shared.LocationTextField
import com.abdownloadmanager.android.pages.add.shared.ShowAddToQueueDialog
import com.abdownloadmanager.android.ui.RenderControlSelections
import com.abdownloadmanager.android.ui.SelectionControlButton
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.android.ui.page.PageTitleWithDescription
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.category.Category
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AddMultiItemPage(
    addMultiDownloadComponent: AndroidAddMultiDownloadComponent,
) {
    val hasSelection = addMultiDownloadComponent.selectionList.isNotEmpty()
    BackHandler(hasSelection) {
        addMultiDownloadComponent.selectAll(false)
    }
    val pageHorizontalPadding = 16.dp
    PageUi(
        modifier = Modifier
            .background(myColors.background)
            .statusBarsPadding(),
        header = {
            val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            PageHeader(
                leadingIcon = {
                    TransparentIconActionButton(
                        MyIcons.back,
                        contentDescription = Res.string.back.asStringSource()
                    ) {
                        backDispatcher?.onBackPressed()
                    }
                },
                headerTitle = {
                    PageTitleWithDescription(
                        title = myStringResource(
                            Res.string.add_download
                        ),
                        description = myStringResource(
                            Res.string.add_multi_download_page_header
                        )
                    )
                }
            )
        },
        footer = {
            Footer(
                Modifier,
                addMultiDownloadComponent,
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(myColors.background)
                .padding(it.paddingValues)
        ) {
            AddMultiDownloadList(
                Modifier.weight(1f),
                addMultiDownloadComponent,
                itePaddingValues = PaddingValues(
                    horizontal = pageHorizontalPadding,
                    vertical = 16.dp,
                )
            )
        }
    }
    val currentDownloadConfigurableList by addMultiDownloadComponent.currentDownloadConfigurableList.collectAsState()
    currentDownloadConfigurableList?.let {
        ExtraConfig(
            onDismiss = {
                addMultiDownloadComponent.openConfigurableList(null)
            },
            configurables = it,
            isOpened = true,
        )
    }
    ShowAddToQueueDialog(
        queueList = addMultiDownloadComponent.queueList.collectAsState().value,
        onQueueSelected = { queue, startQueue ->
            addMultiDownloadComponent.requestAddDownloads(
                queue, startQueue
            )
        },
        onClose = {
            addMultiDownloadComponent.closeAddToQueue()
        },
        isOpened = addMultiDownloadComponent.showAddToQueue,
        newQueueAction = addMultiDownloadComponent.newQueueAction,
    )
}


@Composable
fun Footer(
    modifier: Modifier = Modifier,
    component: AndroidAddMultiDownloadComponent,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(myColors.surface)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onSurface / 0.15f)
        )
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
        ) {
            val total = component.totalList.size
            val showMoreOptions by component.showMoreOptions.collectAsState()
            RenderControlSelections(
                onRequestSelectAll = { component.selectAll(true) },
                onRequestSelectInside = { component.toggleSelectInside() },
                onRequestInvertSelection = { component.inverseSelection() },
                total = total,
                selectionCount = component.selectionList.size,
            ) {
                SelectionControlButton(
                    icon = if (showMoreOptions) {
                        MyIcons.down
                    } else {
                        MyIcons.up
                    },
                    contentDescription = Res.string.more_options.asStringSource(),
                    onClick = {
                        component.setShowMoreOptions(!showMoreOptions)
                    }
                )
            }
            AnimatedVisibility(
                showMoreOptions
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    SaveSettings(
                        modifier = Modifier
                            .fillMaxWidth(),
                        component = component,
                    )
                    val text = component.filterText.collectAsState().value
                    Spacer(Modifier.height(8.dp))
                    MyTextFieldWithIcons(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = text,
                        onTextChange = component::setFilterText,
                        placeholder = myStringResource(Res.string.search),
                        end = {
                            MyTextFieldIcon(
                                MyIcons.clear,
                                enabled = text.isNotEmpty(),
                            ) {
                                component.setFilterText("")
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
            ) {
                val buttonModifier = Modifier.weight(1f)
                PrimaryMainActionButton(
                    text = myStringResource(Res.string.add),
                    onClick = {
                        component.openAddToQueueDialog()
                    },
                    enabled = component.canClickAdd,
                    modifier = buttonModifier,
                )
//                ActionButton(
//                    text = myStringResource(Res.string.cancel),
//                    onClick = {
//                        component.requestClose()
//                    },
//                    modifier = buttonModifier,
//                )
            }
        }
    }
}

@Composable
private fun SaveSettings(
    modifier: Modifier,
    component: AndroidAddMultiDownloadComponent,
) {
    val selectedCategory by component.selectedCategory.collectAsState()

    val folder by component.folder.collectAsState()

    Column(modifier) {
        Text("${myStringResource(Res.string.save_to)}:")
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth()) {
            CategorySaveOption(selectedCategory, component)
            Spacer(Modifier.height(8.dp))
            LocationSaveOption(component, folder)
            Spacer(Modifier)
        }
    }
}

@Composable
private fun LocationSaveOption(
    component: AndroidAddMultiDownloadComponent,
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
private fun CategorySaveOption(
    selectedCategory: Category?,
    component: AndroidAddMultiDownloadComponent
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
                Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth(),
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
                        component.onRequestAddCategory()
                    },
                )
            }
        }
    )
}

@Composable
private fun SaveOption(
    title: String,
    selectedHelp: String,
    unselectedHelp: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    selectedContent: @Composable () -> Unit
) {
    ExpandableItem(
        modifier = Modifier
            .fillMaxWidth(),
        isExpanded = selected,
        header = {
            Row(
                modifier = Modifier
                    .heightIn(mySpacings.thumbSize)
                    .clickable { onSelectedChange(!selected) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckBox(
                    value = selected,
                    onValueChange = onSelectedChange,
                    size = 24.dp
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
