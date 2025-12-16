package com.abdownloadmanager.android.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.pages.about.AboutPage
import com.abdownloadmanager.android.pages.batchdownload.BatchDownloadSheet
import com.abdownloadmanager.android.pages.category.CategorySheet
import com.abdownloadmanager.android.pages.checksum.FileChecksumPage
import com.abdownloadmanager.android.pages.home.HomePage
import com.abdownloadmanager.android.pages.settings.SettingsPage
import com.abdownloadmanager.android.pages.credits.thirdpartylibraries.ThirdPartyLibrariesPage
import com.abdownloadmanager.android.pages.credits.translators.TranslatorsPage
import com.abdownloadmanager.android.pages.editdownload.EditDownloadSheet
import com.abdownloadmanager.android.pages.newqueue.NewQueueSheet
import com.abdownloadmanager.android.pages.onboarding.initialsetup.InitialSetupPage
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionsPage
import com.abdownloadmanager.android.pages.perhostsettings.PerHostSettingsPage
import com.abdownloadmanager.android.pages.queue.QueueConfigSheet
import com.abdownloadmanager.android.pages.updater.UpdaterSheet
import com.abdownloadmanager.shared.ui.widget.NotificationArea
import com.abdownloadmanager.shared.ui.widget.useNotification
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.widget.ScreenSurface
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


@Composable
fun MainContent(
    mainComponent: MainComponent,
) {
    val activity = LocalActivity.current
    val notificationManager = useNotification()
    val scope = rememberCoroutineScope()
    ScreenSurface(
        modifier = Modifier.fillMaxSize(),
        background = myColors.background,
        contentColor = myColors.onBackground
    ) {
        HandleEffects(mainComponent) { effect ->
            when (effect) {
                is MainComponent.MainAppEffects.StartActivity -> {
                    activity?.startActivity(effect.intent)
                }

                is MainComponent.MainAppEffects.SimpleNotificationNotification -> {
                    scope.launch {
                        withTimeout(5000) {
                            notificationManager.showNotification(effect.notificationModel)
                        }
                    }
                }
            }
        }
        Children(
            mainComponent.stack.collectAsState().value,
            modifier = Modifier.imePadding(),
            animation = stackAnimation { scale() + fade() },
        ) {
            when (val screen = it.instance) {
                is Screen.Home -> {
                    HomePage(screen.component)
                }

                is Screen.Settings -> {
                    SettingsPage(screen.component)
                }

                Screen.About -> {
                    AboutPage(
                        onRequestShowOpenSourceLibraries = {
                            mainComponent.openOpenSourceLibrariesPage()
                        },
                        onRequestShowTranslators = {
                            mainComponent.openTranslatorsPage()
                        }
                    )
                }

                Screen.OpenSourceThirdPartyLibraries -> {
                    ThirdPartyLibrariesPage()
                }

                Screen.Translators -> {
                    TranslatorsPage(
                        onBack = {
                            mainComponent.closeTranslatorsPage()
                        }
                    )
                }

                is Screen.PerHostSettings -> {
                    PerHostSettingsPage(component = screen.component)
                }

                is Screen.FileChecksum -> {
                    FileChecksumPage(component = screen.component)
                }

                is Screen.InitialSetup -> {
                    InitialSetupPage(component = screen.component)
                }

                is Screen.Permissions -> {
                    PermissionsPage(component = screen.component)
                }
            }
        }
        CategorySheet(
            mainComponent.categorySlot.rememberChild(),
            mainComponent::closeCategoryDialog
        )
        QueueConfigSheet(
            mainComponent.queueConfigSlot.rememberChild(),
            mainComponent::closeQueues
        )
        NewQueueSheet(
            onQueueCreate = mainComponent::createQueueWithName,
            isOpened = mainComponent.showAddQueue.collectAsState().value,
            onCloseRequest = { mainComponent.setShowAddQueue(false) },
        )
        BatchDownloadSheet(
            component = mainComponent.batchDownloadSlot.rememberChild(),
            onDismiss = mainComponent::closeBatchDownload
        )
        EditDownloadSheet(
            component = mainComponent.editDownloadSlot.rememberChild(),
            onDismiss = mainComponent::closeEditDownloadDialog,
        )
        UpdaterSheet(
            updaterComponent = mainComponent.updaterComponent,
        )
        DisposableEffect(Unit) {
            mainComponent.abdmAppManager.setNotificationsHandledInUi(true)
            onDispose {
                mainComponent.abdmAppManager.setNotificationsHandledInUi(false)
            }
        }
        NotificationArea(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        )
    }
}
