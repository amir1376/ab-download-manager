package com.abdownloadmanager.android.pages.qrcode

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.rememberAppPermissionState
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.asToggledStringSource
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.arkivanov.decompose.retainedComponent
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScannerActivity : ABDMActivity() {

    companion object {
        private const val SCANNED_DATA = "scanned-data"

        val Contract = object : ActivityResultContract<Unit, String?>() {
            override fun createIntent(
                context: Context,
                input: Unit
            ): Intent {
                return Intent(context, ScannerActivity::class.java)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): String? {
                return if (resultCode == RESULT_OK) {
                    intent?.getStringExtra(SCANNED_DATA)
                } else null
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = retainedComponent {
            ScannerComponent(it)
        }
        setABDMContent {
            ScannerSheet(
                component = component,
                onClose = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
                onScanned = {
                    setResult(
                        RESULT_OK,
                        Intent().apply {
                            putExtra(SCANNED_DATA, it)
                        }
                    )
                    finish()
                }
            )
        }
    }
}

@Composable
fun rememberQrCodeScannerLauncher(
    onScanned: (String) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ScannerActivity.Contract
    ) { result ->
        result?.let {
            onScanned(it)
        }
    }
    return {
        launcher.launch(Unit)
    }
}

@Composable
private fun ScannerSheet(
    component: ScannerComponent,
    onClose: () -> Unit,
    onScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val shouldProceedWithCamera = remember {
        CompletableDeferred<Boolean>()
    }
    val permission = rememberAppPermissionState(
        ABDMPermissions.cameraPermission,
        onNewResult = {
            shouldProceedWithCamera.complete(it)
        }
    )
    LaunchedEffect(Unit) {
        if (!permission.isGranted) {
            permission.launchRequest()
        } else {
            shouldProceedWithCamera.complete(true)
        }
    }

    val onClose by rememberUpdatedState(onClose)
    val onScanned by rememberUpdatedState(onScanned)
    HandleEffects(component) {
        when (it) {
            is ScannerComponent.Effects.DataReady -> {
                onScanned(it.data)
            }
        }
    }

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    val state by component.state.collectAsStateWithLifecycle()

    val analysisExecutor = remember {
        Executors.newSingleThreadExecutor()
    }
    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        // wait for permission
        val canWeUse = shouldProceedWithCamera.await()
        if (!canWeUse) {
            onClose()
        }

        val provider = ProcessCameraProvider.getInstance(context).get()
        provider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { req -> surfaceRequest = req }
        }

        val imageAnalysis = ImageAnalysis.Builder().build().apply {
            setAnalyzer(
                analysisExecutor,
                ZxingQRCodeAnalyzer(
                    onSuccess = component::onSuccess,
                    onFailure = {},
                ),
            )
        }

        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis,
        )

        val hasFlash = camera.cameraInfo.hasFlashUnit()
        component.setHasFlashUnit(hasFlash)

        launch {
            component.state.collect { state ->
                camera.cameraControl.enableTorch(state.isFlashlightOn)
            }
        }
    }

    val responsiveDialogState = rememberResponsiveDialogState(false)
    LaunchedEffect(Unit) {
        responsiveDialogState.show()
    }
    val closeDialog = {
        responsiveDialogState.hide()
    }
    responsiveDialogState.OnFullyDismissed(onClose)
    ResponsiveDialog(
        state = responsiveDialogState,
        onDismiss = closeDialog,
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            myStringResource(Res.string.scan_qr_code),
                            icon = MyIcons.qrCode,
                        )
                    },
                    headerActions = {
                        if (state.hasFlashUnit) {
                            TransparentIconActionButton(
                                icon = MyIcons.fast,
                                contentDescription = state.isFlashlightOn.asToggledStringSource(),
                                onClick = { component.toggleFlashlight() },
                                modifier = Modifier.ifThen(
                                    !state.isFlashlightOn
                                ) {
                                    alpha(0.5f)
                                }
                            )
                        }
                        TransparentIconActionButton(
                            icon = MyIcons.close,
                            contentDescription = Res.string.close.asStringSource(),
                            onClick = closeDialog,
                        )
                    }
                )
            },
        ) {
            Box(
                modifier = Modifier,
            ) {
                Box(
                    modifier = Modifier
                ) {
                    surfaceRequest?.let { req ->
                        CameraXViewfinder(
                            surfaceRequest = req,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
