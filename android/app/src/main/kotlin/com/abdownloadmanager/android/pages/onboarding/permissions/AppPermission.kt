package com.abdownloadmanager.android.pages.onboarding.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource

data class AppPermission(
    val title: StringSource,
    val description: StringSource,
    val icon: IconSource,
    val isOptional: Boolean,
    val permissions: List<String>, // Manifest.Permissions
    val permissionRequestFactory: PermissionRequestLauncherFactory = DefaultPermissionRequesterFactory,
    val permissionChecker: PermissionRequestChecker = DefaultPermissionRequestChecker,
)

@Composable
fun rememberAppPermissionState(
    appPermission: AppPermission,
    onNewResult: (Boolean) -> Unit = {},
): AppPermissionState {
    val activity = requireNotNull(LocalActivity.current) {
        "We should query permissions from activity"
    }
    val state = remember(
        appPermission,
        activity,
    ) {
        AppPermissionState(
            appPermission = appPermission,
            context = activity
        )
    }
    val launcher = appPermission.permissionRequestFactory.rememberLauncher(appPermission) {
        state.refreshStatus()
        onNewResult(it)
    }
    ListenForPermissionChangesInLifecycle(appPermission) {
        state.refreshStatus()
    }
    LaunchedEffect(state, launcher) {
        state.setLauncher(launcher)
    }
    return state
}

interface PermissionRequestLauncherFactory {
    @Composable
    fun rememberLauncher(
        appPermission: AppPermission,
        onNewResult: (Boolean) -> Unit
    ): PermissionRequestLauncher
}

interface PermissionRequestLauncher {
    fun launchPermissionRequest()
}

interface PermissionRequestChecker {
    fun isGranted(
        context: Context,
        appPermission: AppPermission,
    ): Boolean

    // in case of deny access
    fun shouldShowRationale(
        activity: Activity,
        appPermission: AppPermission,
    ): Boolean {
        return false
    }
}

sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data class NotGranted(
        val shouldShowRationale: Boolean,
    ) : PermissionStatus
}

class AppPermissionState(
    val appPermission: AppPermission,
    private val context: Activity,
) {

    var permissionStatus by mutableStateOf(checkGranted())
        private set

    fun refreshStatus() {
        permissionStatus = checkGranted()
    }

    fun checkGranted(): PermissionStatus {
        val permissionChecker = appPermission.permissionChecker
        return when {
            permissionChecker.isGranted(context, appPermission) -> PermissionStatus.Granted
            else -> PermissionStatus.NotGranted(
                permissionChecker.shouldShowRationale(context, appPermission),
            )
        }
    }

    val isGranted by derivedStateOf {
        permissionStatus == PermissionStatus.Granted
    }
    val requiresRational by derivedStateOf {
        permissionStatus.let {
            it is PermissionStatus.NotGranted && it.shouldShowRationale
        }
    }

    private var launcher: PermissionRequestLauncher? = null
    fun setLauncher(requestLauncher: PermissionRequestLauncher) {
        launcher = requestLauncher
    }

    fun launchRequest() {
        launcher?.launchPermissionRequest()
    }
}


object DefaultPermissionRequesterFactory : PermissionRequestLauncherFactory {
    @Composable
    override fun rememberLauncher(
        appPermission: AppPermission,
        onNewResult: (Boolean) -> Unit
    ): PermissionRequestLauncher {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            onNewResult(it.all { entry -> entry.value })
        }
        return remember(appPermission) {
            object : PermissionRequestLauncher {
                override fun launchPermissionRequest() {
                    launcher.launch(appPermission.permissions.toTypedArray())
                }
            }
        }
    }
}

@Composable
private fun ListenForPermissionChangesInLifecycle(
    appPermission: AppPermission,
    onRequestRefreshStatus: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(appPermission, context, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRequestRefreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


object DefaultPermissionRequestChecker : PermissionRequestChecker {
    override fun isGranted(
        context: Context,
        appPermission: AppPermission
    ): Boolean {
        return appPermission.permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun shouldShowRationale(
        activity: Activity,
        appPermission: AppPermission
    ): Boolean {
        return appPermission.permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }
}
