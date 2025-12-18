package com.abdownloadmanager.android.pages.perhostsettings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.ui.configurable.RenderConfigurableGroup
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.page.PageHeader
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.android.ui.page.PageUi
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.perhostsettings.PerHostSettingsItemWithId
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.modifiers.autoMirror
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlinx.coroutines.*

@Composable
fun PerHostSettingsPage(component: AndroidPerHostSettingsComponent) {
    val perHostSettings by component.editedPerHostSettings.collectAsState()
    val selectedItemId by component.selectedId.collectAsState()
//    WindowTitle(myStringResource(Res.string.settings_per_host_settings))
    val configurableList = component.selectedItemConfigurableList.collectAsState().value
    val canSave by component.canSave.collectAsState()
    val scope = rememberCoroutineScope()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current
    BackHandler(
        configurableList != null
    ) {
        component.reset()
    }
    PageUi(
        header = {
            PageHeader(
                leadingIcon = {
                    TransparentIconActionButton(
                        icon = MyIcons.back,
                        contentDescription = myStringResource(Res.string.back),
                        onClick = {
                            backDispatcher?.onBackPressedDispatcher?.onBackPressed()
                        }
                    )
                },
                headerTitle = {
                    PageTitle(myStringResource(Res.string.settings_per_host_settings))
                },
                headerActions = {
                    if (configurableList == null) {
                        TransparentIconActionButton(
                            icon = MyIcons.add,
                            contentDescription = myStringResource(Res.string.add),
                            onClick = {
                                component.onRequestAddNewHostSettingsItem()
                            }
                        )
                    } else {
                        TransparentIconActionButton(
                            icon = MyIcons.remove,
                            contentDescription = myStringResource(Res.string.remove),
                            onClick = {
                                component.onRequestDeleteConfig(configurableList.id)
                            }
                        )
                        TransparentIconActionButton(
                            icon = MyIcons.check,
                            enabled = canSave,
                            onClick = {
                                scope.launch {
                                    component.saveAndReturn()
                                }
                            },
                            contentDescription = myStringResource(Res.string.update)
                        )
                    }
                }
            )
        },
        footer = {

        },
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding()
    ) {
        // TODO improvement make it tablet friendly
        AnimatedContent(
            configurableList,
            modifier = Modifier.padding(it.paddingValues)
        ) { configurableList ->
            if (configurableList != null) {
                RenderPerHostSettingsItem(
                    modifier = Modifier
                        .padding(8.dp),
                    itemId = configurableList.id,
                    configurableList = configurableList.configurableGroups,
                )
            } else {
                Column {
                    HostList(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        hosts = perHostSettings,
                        selectedId = selectedItemId,
                        setSelected = { id ->
                            component.onIdSelected(id)
                        },
                        component = component
                    )
                }
            }
        }
    }
}


@Composable
private fun RenderPerHostSettingsItem(
    modifier: Modifier,
    itemId: String,
    configurableList: List<ConfigurableGroup>,
) {
    val fm = LocalFocusManager.current
    //remove focus to prevent accidentally change config in different queue
    LaunchedEffect(itemId) {
        fm.clearFocus()
    }
    Column(modifier) {
        val pageModifier = Modifier
            .fillMaxSize()
        RenderPerHostSettingsConfigurableGroup(pageModifier, configurableList)
    }
}

@Composable
private fun RenderPerHostSettingsConfigurableGroup(
    modifier: Modifier,
    configurableGroups: List<ConfigurableGroup>,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
    ) {
        for ((index, cfgGroup) in configurableGroups.withIndex()) {
            RenderConfigurableGroup(
                group = cfgGroup,
                modifier = Modifier,
                itemPadding = PaddingValues(
                    vertical = 8.dp,
                    horizontal = 16.dp
                )
            )
            if (index != configurableGroups.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HostList(
    modifier: Modifier,
    hosts: List<PerHostSettingsItemWithId>,
    selectedId: String?,
    setSelected: (String) -> Unit,
    component: AndroidPerHostSettingsComponent,
) {
    val shape = myShapes.defaultRounded
    val borderColor = myColors.surface / 0.5f
    var search by remember { mutableStateOf("") }
    val defaultEmptyName = myStringResource(Res.string.settings_per_host_settings_new_host)
    val filteredHosts = remember(hosts, search) {
        hosts.ifThen(search.isNotEmpty()) {
            filter {
                it.perHostSettingsItem.host.contains(search, true)
            }
        }
    }
    Column(
        modifier
            .border(1.dp, borderColor, shape)
            .clip(shape)
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn {
                items(filteredHosts, key = { it.id }) { s ->
                    val isSelected = selectedId == s.id
                    SideBarItem(
                        isSelected = isSelected,
                        onClick = { setSelected(s.id) },
                        name = s.perHostSettingsItem.host.takeIf { it.isNotBlank() } ?: defaultEmptyName,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (filteredHosts.isEmpty()) {
                WithContentAlpha(0.75f) {
                    Text(
                        myStringResource(Res.string.list_is_empty),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

//        Row(
//            modifier = Modifier
//                .padding(vertical = 4.dp)
//                .padding(horizontal = 8.dp)
//                .height(IntrinsicSize.Max)
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.End
//        ) {
//            SearchBox(
//                search,
//                onTextChange = {
//                    search = it
//                },
//                placeholder = myStringResource(Res.string.search),
//                modifier = Modifier.weight(1f).fillMaxHeight(),
//            )
//        }
    }
}

@Composable
private fun SideBarItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(IntrinsicSize.Max)
            .heightIn(mySpacings.thumbSize)
            .ifThen(isSelected) {
                background(myColors.onBackground / 0.05f)
            }
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .padding(vertical = 8.dp)
                .padding(start = 16.dp)
                .padding(end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WithContentAlpha(if (isSelected) 1f else 0.75f) {
                Text(
                    name,
                    Modifier.weight(1f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = myTextSizes.lg,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
        AnimatedVisibility(
            isSelected,
            modifier = Modifier
                .align(Alignment.CenterStart),
            enter = scaleIn(),
            exit = scaleOut(),
        ) {
            Spacer(
                Modifier
                    .height(16.dp)
                    .width(3.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 12.dp,
                            topEnd = 12.dp,
                        )
                    )
                    .background(myColors.primary)
            )
        }
        if (isSelected) {
            listOf(
                Alignment.TopCenter,
                Alignment.BottomCenter,
            ).forEach {
                Spacer(
                    Modifier
                        .align(it)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    myColors.onBackground / 0.1f,
                                    myColors.onBackground / 0.1f,
                                    Color.Transparent,
                                )
                            )
                        )
                )
            }
        }
    }
}
