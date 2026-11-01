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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
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
import com.google.zxing.ResultPoint
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

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
    val scope = rememberCoroutineScope()
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
    val containerSize = remember {
        mutableStateOf(Size.Zero)
    }
    val detectedQrBounds = remember {
        mutableStateOf(null as? Rect)
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

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(
                    analysisExecutor,
                    ZxingQRCodeAnalyzer(
                        onSuccess = { result, imageWidth, imageHeight, rotation ->
                            val text = result.text
                            scope.launch {
                                detectedQrBounds.value = result.resultPoints?.let {
                                    mapQrPointsToScreen(
                                        points = it,
                                        imageWidth = imageWidth,
                                        imageHeight = imageHeight,
                                        rotationDegrees = rotation,
                                        containerWidth = containerSize.value.width,
                                        containerHeight = containerSize.value.height
                                    )
                                }
                                delay(1.seconds) // to see qr box overlay on camera movement
                                component.onSuccess(text)
                            }
                        },
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .onSizeChanged {
                        containerSize.value = it.toSize()
                    }
            ) {
                surfaceRequest?.let { req ->
                    CameraXViewfinder(
                        surfaceRequest = req,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        implementationMode = ImplementationMode.EMBEDDED
                    )
                    QRBoxOverlay(qrRect = detectedQrBounds.value)
                }

            }
        }
    }
}

@Composable
private fun QRBoxOverlay(qrRect: Rect? = null) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val containerWidth = with(density) { maxWidth.toPx() }
        val containerHeight = with(density) { maxHeight.toPx() }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val minQrBoxSizePx = with(density) { 200.dp.toPx() }
        val maxQrBoxSizePx = with(density) { 210.dp.toPx() }
        val qrBoxSize by infiniteTransition.animateFloat(
            initialValue = minQrBoxSizePx,
            targetValue = maxQrBoxSizePx,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "qrBoxPulse"
        )

        // Default pulsing center box when no QR code is detected
        val defaultRect = remember(containerWidth, containerHeight, qrBoxSize) {
            val left = (containerWidth - qrBoxSize) / 2f
            val top = (containerHeight - qrBoxSize) / 2f
            Rect(
                left = left,
                top = top,
                right = left + qrBoxSize,
                bottom = top + qrBoxSize
            )
        }

        // Smoothly animate the cutout rect from the center box to the detected QR code (and back)
        val targetRect = qrRect ?: defaultRect
        val animatedRect by animateRectAsState(
            targetValue = targetRect,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "qrRectAnimation"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val left = animatedRect.left
            val top = animatedRect.top
            val right = animatedRect.right
            val bottom = animatedRect.bottom

            val boxWidth = right - left
            val boxHeight = bottom - top
            val minSide = minOf(boxWidth, boxHeight)

            // Adjust cornerRadius and cornerLength dynamically if box size changes
            val cornerRadius = minOf(12.dp.toPx(), minSide / 4f)
            val cornerLength = minOf(20.dp.toPx(), minSide / 3f).coerceAtLeast(cornerRadius)
            val strokeWidth = 3.dp.toPx()

            val path = Path().apply {
                // 1. Full-screen outer rectangle
                addRect(Rect(Offset.Zero, size))
                // 2. Center cutout rectangle with rounded corners
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                )
                fillType = PathFillType.EvenOdd
            }
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
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun mapQrPointsToScreen(
    points: Array<ResultPoint>,
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int,
    containerWidth: Float,
    containerHeight: Float
): Rect {
    val (uprightWidth, uprightHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
        imageHeight to imageWidth
    } else {
        imageWidth to imageHeight
    }
    val scale = maxOf(
        containerWidth / uprightWidth.toFloat(),
        containerHeight / uprightHeight.toFloat()
    )
    val scaledWidth = uprightWidth * scale
    val scaledHeight = uprightHeight * scale
    val offsetX = (containerWidth - scaledWidth) / 2f
    val offsetY = (containerHeight - scaledHeight) / 2f

    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    for (p in points) {
        val (rotatedX, rotatedY) = when (rotationDegrees) {
            90 -> (imageHeight - p.y) to p.x
            180 -> (imageWidth - p.x) to (imageHeight - p.y)
            270 -> p.y to (imageWidth - p.x)
            else -> p.x to p.y
        }
        val screenX = rotatedX * scale + offsetX
        val screenY = rotatedY * scale + offsetY
        minX = minOf(minX, screenX)
        maxX = maxOf(maxX, screenX)
        minY = minOf(minY, screenY)
        maxY = maxOf(maxY, screenY)
    }
    val padding = 16f * scale.coerceAtLeast(1f)
    return Rect(
        left = minX - padding,
        top = minY - padding,
        right = maxX + padding,
        bottom = maxY + padding
    )
}
