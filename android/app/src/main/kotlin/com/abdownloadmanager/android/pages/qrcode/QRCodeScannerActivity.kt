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
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.rememberAppPermissionState
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetType
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
            type = SheetType.Overlay
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)) {
                surfaceRequest?.let { req ->
                    CameraXViewfinder(
                        surfaceRequest = req,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        implementationMode = ImplementationMode.EMBEDDED
                    )
                }

                QRBoxOverlay()
            }
        }
    }
}

@Composable
private fun QRBoxOverlay() {
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current
    val minQrBoxSizePx = with(density) { 200.dp.toPx() }
    val maxQrBoxSizePx = with(density) { 210.dp.toPx() }
    val qrBoxSize by infiniteTransition.animateFloat(
        minQrBoxSizePx, maxQrBoxSizePx, animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cornerRadius = 12.dp.toPx()
        val left = (size.width - qrBoxSize) / 2
        val top = (size.height - qrBoxSize) / 2
        val right = left + qrBoxSize
        val bottom = top + qrBoxSize
        val path = Path().apply {
            // 1. Full-screen outer rectangle
            addRect(Rect(Offset.Zero, size))
            // 2. Center cutout rectangle with rounded corners
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset(left, top), Size(qrBoxSize, qrBoxSize)),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            )
            fillType = PathFillType.EvenOdd
        }
        // Corner reticle configurations
        val cornerLength = 20.dp.toPx() // Length of each corner arm
        val strokeWidth = 3.dp.toPx()   // Thickness of the white corner lines
        // Build path for the 4 rounded corner reticles matching the cutout radius
        val cornersPath = Path().apply {
            val arcSize = cornerRadius * 2
            // Top-Left corner
            moveTo(left, top + cornerLength)
            arcTo(
                rect = Rect(left, top, left + arcSize, top + arcSize),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left + cornerLength, top)
            // Top-Right corner
            moveTo(right - cornerLength, top)
            arcTo(
                rect = Rect(right - arcSize, top, right, top + arcSize),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right, top + cornerLength)
            // Bottom-Right corner
            moveTo(right, bottom - cornerLength)
            arcTo(
                rect = Rect(right - arcSize, bottom - arcSize, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right - cornerLength, bottom)
            // Bottom-Left corner
            moveTo(left + cornerLength, bottom)
            arcTo(
                rect = Rect(left, bottom - arcSize, left + arcSize, bottom),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left, bottom - cornerLength)
        }

        drawPath(
            path = path,
            color = Color.Black.copy(0.8f)
        )
        drawPath(
            path = cornersPath,
            color = Color.White,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,  // Rounded line caps
                join = StrokeJoin.Round // Rounded joints
            )
        )
    }
}
