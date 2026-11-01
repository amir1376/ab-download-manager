package com.abdownloadmanager.android.pages.qrcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.nio.ByteBuffer

class ZxingQRCodeAnalyzer(
    private val onSuccess: ((result: Result, imageWidth: Int, imageHeight: Int, rotation: Int) -> Unit),
    private val onFailure: ((Exception) -> Unit),
) : ImageAnalysis.Analyzer {

    private val qrCodeReader = QRCodeReader()

    private val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    private var reusableBuffer: ByteArray? = null

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes[0]
            val byteBuffer: ByteBuffer = plane.buffer
            val rowStride = plane.rowStride
            val height = image.height
            val width = image.width

            val requiredSize = byteBuffer.remaining()
            var buffer = reusableBuffer
            if (buffer == null || buffer.size < requiredSize) {
                buffer = ByteArray(requiredSize)
                reusableBuffer = buffer
            }
            byteBuffer.get(buffer, 0, requiredSize)

            // Read directly from the grayscale layer without unnecessary Bitmap conversion
            val source = PlanarYUVLuminanceSource(
                buffer,
                rowStride,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val result = try {
                qrCodeReader.decode(binaryBitmap, hints)
            } catch (_: NotFoundException) {
                try {
                    qrCodeReader.decode(BinaryBitmap(HybridBinarizer(source.invert())), hints)
                } catch (_: NotFoundException) {
                    return
                }
            } finally {
                qrCodeReader.reset()
            }

            onSuccess(result, width, height, image.imageInfo.rotationDegrees)
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            image.close()
        }
    }
}