package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.div
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

private val LocalNotification = compositionLocalOf<NotificationManager> {
    error("LocalNotification not provided yet")
}

@Composable
fun useNotification(): NotificationManager {
    return LocalNotification.current
}

sealed interface NotificationType {
    data class Loading(val percent: Int? = null) : NotificationType
    data object Success : NotificationType
    data object Info : NotificationType
    data object Error : NotificationType
    data object Warning : NotificationType
}

@Stable
class NotificationModel(
    val tag: Any,
    initialTitle: StringSource = "".asStringSource(),
    initialDescription: StringSource = "".asStringSource(),
    initialNotificationType: NotificationType = NotificationType.Info,
) {
    var notificationType: NotificationType by mutableStateOf(initialNotificationType)
    var title: StringSource by mutableStateOf(initialTitle)
    var description: StringSource by mutableStateOf(initialDescription)
}

@Composable
fun ProvideNotificationManager(
    notificationManager: NotificationManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNotification provides notificationManager
    ) {
        content()
    }
}

@Composable
fun ShowNotification(
    title: StringSource,
    description: StringSource,
    type: NotificationType,
    tag: Any = currentCompositeKeyHash,
) {
    val notification = remember(tag) {
        NotificationModel(
            tag = tag,
            initialTitle = title,
            initialDescription = description,
        )
    }
    LaunchedEffect(type){
        notification.notificationType=type
    }
    LaunchedEffect(title) {
        notification.title = title
    }
    LaunchedEffect(description) {
        notification.description = description
    }
    val notificationManager = useNotification()
    LaunchedEffect(Unit) {
        notificationManager.showNotification(notification)
    }
}

@Composable
fun NotificationArea(
    modifier: Modifier
) {
    val notificationManager = useNotification()
//    val list = notificationManager.activeNotificationList
    val activeNotificationList by notificationManager.activeNotificationList.collectAsState()
    val notificationListToShow by remember {
        derivedStateOf {
            activeNotificationList.distinctBy { it.tag }
        }
    }
    LazyColumn (modifier) {
        itemsIndexed(notificationListToShow) { index, item ->
            Spacer(Modifier.size(12.dp))
            RenderNotification(
                Modifier.animateItem(),
                item
            )
            Spacer(Modifier.size(12.dp))
        }
    }
}

@Composable
private fun RenderNotification(
    modifier: Modifier,
    notificationModel: NotificationModel,
    alignment: Alignment = Alignment.BottomCenter,
) {
    val shape = myShapes.defaultRounded
    Row(modifier
        .shadow(4.dp, shape)
        .animateContentSize(alignment = alignment)
        .height(IntrinsicSize.Max)
        .fillMaxWidth()
        .clip(shape)
        .border(1.dp, myColors.menuBorderColor, shape)
        .background(myColors.menuGradientBackground, shape)
        .padding(10.dp)
    ) {
        NotificationIcon(
            Modifier
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterVertically),
            notificationModel
        )
        Spacer(Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp)
            .padding(vertical = 2.dp)
            .width(1.dp)
            .background(myColors.onSurface/20)
        )
        Column {
            NotificationTitle(notificationModel)
            Spacer(Modifier.size(4.dp))
            NotificationDescription(notificationModel)
        }
    }
}

@Composable
private fun NotificationDescription(notificationModel: NotificationModel) {
    WithContentAlpha(0.75f) {
        Text(
            text = notificationModel.description.rememberString(),
            fontSize = myTextSizes.base
        )
    }
}

@Composable
private fun NotificationTitle(notificationModel: NotificationModel) {
    WithContentAlpha(1f) {
        Text(
            text = notificationModel.title.rememberString(),
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun LoadingIcon(modifier: Modifier, percent: Int?) {
    if (percent == null) {
        LoadingIndicator(
            modifier = modifier
        )
    } else {
        LoadingIndicator(
            modifier = modifier,
            progress = (percent / 100f).coerceIn(0f, 1f)
        )
    }
}

@Composable
private fun InfoIcon(modifier: Modifier, color: Color) {
    MyIcon(
        icon = MyIcons.info,
        contentDescription = null,
        modifier = modifier,
        tint = color,
    )
}

@Composable
fun NotificationIcon(
    modifier: Modifier=Modifier,
    notificationModel: NotificationModel
) {
    val notificationType = notificationModel.notificationType
    val modifier = modifier.size(24.dp)
    when (notificationType) {
        NotificationType.Error -> {
            InfoIcon(modifier, myColors.error)
        }

        NotificationType.Info -> {
            InfoIcon(modifier, myColors.info)
        }

        NotificationType.Success -> {
            InfoIcon(modifier, myColors.success)
        }

        NotificationType.Warning -> {
            InfoIcon(modifier, myColors.warning)
        }

        is NotificationType.Loading -> {
            LoadingIcon(modifier, notificationType.percent)
        }
    }
}


@Stable
class NotificationManager {
    private val _activeNotificationList = MutableStateFlow<List<NotificationModel>>(emptyList())
    val activeNotificationList = _activeNotificationList.asStateFlow()

    suspend fun showNotification(
        notification: NotificationModel,
    ) {
        try {
            _activeNotificationList.update {
                it.plus(notification)
            }
            awaitCancellation()
        } finally {
            _activeNotificationList.update {
                it.minus(notification)
            }
        }
    }

    suspend fun showNotification(
        title: StringSource,
        description: StringSource,
        delay: Long = -1,
        type: NotificationType = NotificationType.Info,
        tag: Any = UUID.randomUUID(),
    ) {
        val notification = NotificationModel(
            tag = tag,
            initialTitle = title,
            initialDescription = description,
            initialNotificationType = type,
        )
        coroutineScope {
            if (delay == -1L) {
                showNotification(notification)
            } else {
                withTimeoutOrNull(delay) {
                    showNotification(notification)
                }
            }
        }
    }
}

/*
fun main() {
    application {
        ABDownloaderTheme("dark") {
            ProvideNotificationManager {
                CustomWindow(
                    rememberWindowState(
                        size = DpSize(400.dp, 400.dp)
                    ),
                    onCloseRequest = this::exitApplication,
                ) {
                    val useNotification = useNotification()
                    LaunchedEffect(Unit) {
                        delay(1000)
                        launch {
                            useNotification.showNotification(
                                title = "A title",
                                description = "A brief description",
                                delay = 5000,
                            )
                        }
                        delay(1000)
                        launch {
                            useNotification.showNotification(
                                title = "A second title",
                                description = "A brief description",
                                delay = 5000,
                            )
                        }
                    }

                    Box(Modifier.fillMaxSize()) {
                        NotificationArea(
                            Modifier
                                .width(200.dp)
                                .padding(8.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}*/
