package com.abdownloadmanager.android.pages.checksum

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.configurable.SheetInput
import com.abdownloadmanager.android.ui.page.PageTitle
import com.abdownloadmanager.shared.ui.configurable.item.FileChecksumConfigurable
import com.abdownloadmanager.shared.ui.configurable.RenderSpinner
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.ui.configurable.RenderConfigurable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.checksum.ChecksumStatus
import com.abdownloadmanager.shared.pages.checksum.DownloadItemWithChecksum
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Help
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.FileChecksum
import com.abdownloadmanager.shared.util.FileChecksumAlgorithm
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberDotLoading
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FileChecksumPage(component: AndroidFileChecksumComponent) {
    val pageTitle = myStringResource(Res.string.file_checksum_page)
    val horizontalPadding = mySpacings.largeSpace
    Column(
        Modifier
            .background(myColors.background)
            .statusBarsPadding()
    ) {
        PageTitle(pageTitle)
        ItemsToBeChecked(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = horizontalPadding),
            component,
        )
        Actions(
            Modifier,
            component,
        )
    }
}

@Composable
fun ItemsToBeChecked(
    modifier: Modifier = Modifier,
    component: AndroidFileChecksumComponent,
) {
    val dividerColor = myColors.onBackground / 0.5f
    val collectAsState by component.state.collectAsState()
    var currentEditingItem: DownloadItemWithChecksum? by remember { mutableStateOf(null) }
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(collectAsState.items) { index, item ->
            val isFirstItem = index == 0
            RenderDownloadItemWithChecksum(
                item = item,
                iconProvider = component.iconProvider,
                onRequestUpdateChecksum = {
                    currentEditingItem = item
                },
                modifier = Modifier.ifThen(!isFirstItem) {
                    drawBehind {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    dividerColor,
                                    Color.Transparent,
                                )
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, 0f)
                        )
                    }
                }
            )
        }
    }
    currentEditingItem?.let { item ->
        FileChecksumTableCellRenderers.ChecksumEditSheet(
            item = item,
            onCloseRequest = {
                currentEditingItem = null
            },
            onRequestSaveNewChecksum = {
                component.updateChecksum(item.downloadItem.id, it)
                currentEditingItem = null
            }
        )
    }
}

@Composable
private fun Actions(
    modifier: Modifier,
    component: AndroidFileChecksumComponent,
) {
    val uiState by component.state.collectAsState()
    Column(
        modifier
            .fillMaxWidth()
            .background(myColors.surface)
            .navigationBarsPadding()
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = myStringResource(Res.string.file_checksum_page_file_checksum_default_algorithm)
                    )
                    Spacer(Modifier.width(8.dp))
                    Help(
                        myStringResource(Res.string.file_checksum_page_file_checksum_default_algorithm_help)
                    )
                }
                Spacer(Modifier.size(8.dp))
                RenderSpinner(
                    modifier = Modifier,
                    possibleValues = FileChecksumAlgorithm.all(),
                    value = uiState.defaultAlgorithm,
                    enabled = !uiState.isChecking,
                    onSelect = {
                        component.onAlgorithmChange(it)
                    },
                    render = {
                        Text(it.algorithm)
                    })
            }
            Spacer(Modifier.height(8.dp))
            Row {
                ActionButton(
                    myStringResource(Res.string.start),
                    onClick = component::onRequestStartCheck,
                    enabled = !uiState.isChecking,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    myStringResource(Res.string.close),
                    onClick = component::onRequestClose,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

}


private data object FileChecksumTableCellRenderers {
    @Composable
    fun RenderStatus(item: DownloadItemWithChecksum) {
        when (val status = item.checksumStatus) {
            is ChecksumStatus.Checking -> {
                RenderCheckingStatus(status.percent)
            }

            ChecksumStatus.Error.DownloadNotFinished -> {
                RenderErrorStatus(myStringResource(Res.string.download_not_finished))
            }

            is ChecksumStatus.Error.Exception -> {
                RenderErrorStatus(status.t.localizedMessage ?: status.t::class.simpleName.orEmpty())
            }

            ChecksumStatus.Error.FileNotFound -> {
                RenderErrorStatus(myStringResource(Res.string.file_not_found))
            }

            is ChecksumStatus.Finished -> {
                RenderFinishedStatus(
                    status = status,
                )
            }

            ChecksumStatus.Waiting -> {
                RenderWaitingStatus()
            }
        }
    }

    @Composable
    fun RenderCalculatedChecksum(item: DownloadItemWithChecksum) {
        val calculatedChecksum = item.calculatedChecksum
        ColumnKeyValue(
            modifier = Modifier,
            keyContent = {
                RenderKey {
                    Text(myStringResource(Res.string.calculated_checksum))
                }
            },
            valueContent = {
                if (calculatedChecksum != null) {
                    SimpleText(calculatedChecksum)
                } else if (item.isProcessing) {
                    //shimmer
                    ShimmerEffect(
                        centerColor = myColors.onBackground / 0.4f,
                        surroundingColor = myColors.onBackground / 0.1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(myShapes.defaultRounded)
                            .height(myTextSizes.base.value.dp)
                    )
                } else if (item.isError) {
                    SimpleText("!")
                }
            },
            actions = {

                TransparentIconActionButton(
                    enabled = calculatedChecksum != null,
                    icon = MyIcons.copy,
                    contentDescription = Res.string.copy.asStringSource(),
                    onClick = {
                        item.calculatedChecksum
                            ?.let { savedChecksum -> ClipboardUtil.copy(savedChecksum) }
                    },
                )
            }
        )
    }

    @Composable
    fun RenderSavedChecksum(
        item: DownloadItemWithChecksum,
        onRequestUpdateChecksum: () -> Unit,
    ) {
        ColumnKeyValue(
            modifier = Modifier,
            keyContent = {
                RenderKey {
                    Text(myStringResource(Res.string.saved_checksum))
                }
            },
            valueContent = {
                Text(item.savedChecksum.orEmpty())
            },
            actions = {
                TransparentIconActionButton(
                    icon = MyIcons.edit,
                    contentDescription = Res.string.edit.asStringSource(),
                    onClick = onRequestUpdateChecksum,
                )
                TransparentIconActionButton(
                    icon = MyIcons.copy,
                    contentDescription = Res.string.copy.asStringSource(),
                    enabled = item.savedChecksum != null,
                    onClick = {
                        item.savedChecksum
                            ?.let { savedChecksum -> ClipboardUtil.copy(savedChecksum) }
                    },
                )
            }
        )
    }

    @Composable
    fun ChecksumEditSheet(
        item: DownloadItemWithChecksum,
        onCloseRequest: () -> Unit,
        onRequestSaveNewChecksum: (FileChecksum?) -> Unit,
    ) {
        val editChecksumFlow = remember(item) {
            MutableStateFlow<FileChecksum?>(FileChecksum(item.algorithm, item.savedChecksum.orEmpty()))
        }
        val fileChecksumConfigurable = remember(item) {
            FileChecksumConfigurable(
                title = Res.string.download_item_settings_file_checksum.asStringSource(),
                description = Res.string.download_item_settings_file_checksum_description.asStringSource(),
                backedBy = editChecksumFlow,
                describe = {
                    "".asStringSource()
                },
            )
        }
        SheetInput(
            configurable = fileChecksumConfigurable,
            isOpened = true,
            onDismiss = onCloseRequest,
            onConfirm = onRequestSaveNewChecksum,
        ) {
            RenderConfigurable(
                fileChecksumConfigurable,
                ConfigurableUiProps(
                    modifier = it.modifier,
                ),
            )
        }
    }

    @Composable
    private fun ShimmerEffect(
        modifier: Modifier = Modifier,
        centerColor: Color = Color.Gray,
        surroundingColor: Color = Color.Gray,
    ) {
        val transition = rememberInfiniteTransition()
        val translateAnim = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000,
                    easing = LinearEasing
                )
            )
        )

        val brush = Brush.linearGradient(
            colors = listOf(
                surroundingColor,
                centerColor,
                surroundingColor,
            ),
            start = Offset(0f, 0f),
            end = Offset(translateAnim.value, 0f)
        )

        Box(
            modifier = modifier
                .background(brush = brush)
        )
    }

    @Composable
    private fun RenderErrorStatus(message: String) {
        IconWithText(
            icon = MyIcons.info,
            text = message,
            color = myColors.error,
        )
    }

    @Composable
    private fun RenderFinishedStatus(
        status: ChecksumStatus.Finished,
    ) {
        val text: StringSource
        val color: Color
        val icon: IconSource
        when (status) {
            ChecksumStatus.Finished.Done -> {
                text = Res.string.done.asStringSource()
                icon = MyIcons.check
                color = myColors.info
            }

            ChecksumStatus.Finished.Matches -> {
                text = Res.string.matches.asStringSource()
                icon = MyIcons.check
                color = myColors.success
            }

            ChecksumStatus.Finished.NotMatches -> {
                text = Res.string.not_matches.asStringSource()
                icon = MyIcons.info
                color = myColors.warning
            }
        }
        IconWithText(
            icon = icon,
            text = text.rememberString(),
            color = color,
        )
    }

    @Composable
    private fun IconWithText(
        icon: IconSource,
        text: String,
        color: Color,
    ) {
        WithContentColor(color) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MyIcon(
                    icon,
                    modifier = Modifier.size(16.dp),
                    contentDescription = null,
                )
                Spacer(Modifier.width(2.dp))
                SimpleText(text)
            }
        }
    }

    @Composable
    private fun RenderCheckingStatus(percent: Int) {
        Column {
            ProgressStatus(percent, myColors.primaryGradient)
        }
    }

    @Composable
    private fun RenderWaitingStatus() {
        Row {
            SimpleText("${myStringResource(Res.string.waiting)} ${rememberDotLoading()}")
        }
    }

    @Composable
    private fun ProgressStatus(
        percent: Int?,
        background: Brush = myColors.primaryGradient,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(myColors.surface)
        ) {
            if (percent != null) {
                val w = (percent / 100f).coerceIn(0f..1f)
                Spacer(
                    Modifier
                        .height(5.dp)
                        .fillMaxWidth(
                            animateFloatAsState(
                                w, tween(100)
                            ).value
                        )
                        .background(background)
                )
            }
        }
    }

    @Composable
    private fun SimpleText(string: String, modifier: Modifier = Modifier) {
        Text(
            string,
            modifier = modifier,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
        )
    }

    @Composable
    fun ColumnKeyValue(
        modifier: Modifier,
        keyContent: @Composable () -> Unit,
        valueContent: @Composable () -> Unit,
        actions: @Composable () -> Unit,
    ) {
        Row(modifier) {
            Column(Modifier.weight(1f)) {
                RenderKey { keyContent() }
                Space()
                RenderValue { valueContent() }
            }
            Space()
            actions()
        }
    }

    @Composable
    fun Space() {
        Spacer(Modifier.size(mySpacings.mediumSpace))
    }

    @Composable
    fun RenderKey(
        content: @Composable () -> Unit
    ) {
        WithContentAlpha(0.5f) {
            content()
        }
    }

    @Composable
    fun RenderValue(
        content: @Composable () -> Unit
    ) {
        WithContentAlpha(1f) {
            content()
        }
    }

    @Composable
    fun RowKeyValue(
        key: String,
        value: String
    ) {
        Row {
            RenderKey {
                Text(key)
            }
            Space()
            RenderValue {
                Text(value)
            }
        }
    }
}

@Composable
private fun RenderDownloadItemWithChecksum(
    item: DownloadItemWithChecksum,
    iconProvider: FileIconProvider,
    onRequestUpdateChecksum: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.padding(
            mySpacings.largeSpace,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyIcon(
                iconProvider.rememberIcon(item.downloadItem.name),
                modifier = Modifier.size(24.dp),
                contentDescription = null,
            )
            Spacer(Modifier.width(mySpacings.mediumSpace))
            Column {
                Text(item.downloadItem.name)
                Spacer(Modifier.height(mySpacings.mediumSpace))
                FileChecksumTableCellRenderers.RowKeyValue(
                    key = myStringResource(Res.string.checksum_algorithm),
                    value = item.algorithm
                )
            }
        }
        Spacer(Modifier.height(mySpacings.mediumSpace))
        FileChecksumTableCellRenderers.RenderSavedChecksum(
            item = item,
            onRequestUpdateChecksum = onRequestUpdateChecksum
        )
        Spacer(Modifier.height(mySpacings.mediumSpace))
        FileChecksumTableCellRenderers.RenderCalculatedChecksum(item)
        Spacer(Modifier.height(mySpacings.mediumSpace))
        FileChecksumTableCellRenderers.RenderStatus(item)
    }
}
