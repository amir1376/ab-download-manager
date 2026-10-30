package com.abdownloadmanager.android.pages.qrcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader


class ZxingQRCodeAnalyzer(
    private val onSuccess: ((String) -> Unit),
    private val onFailure: ((Exception) -> Unit),
) : ImageAnalysis.Analyzer {
    private val qrCodeReader = QRCodeReader()
    override fun analyze(image: ImageProxy) {
        try {
            val bitmap = image.toBitmap()
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(
                intArray,
                0,
                bitmap.width,
                0,
                0,
                bitmap.width,
                bitmap.height,
            )
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val result = try {
                qrCodeReader.decode(BinaryBitmap(GlobalHistogramBinarizer(source)))
            } catch (_: NotFoundException) {
                try {
                    qrCodeReader.decode(BinaryBitmap(GlobalHistogramBinarizer(source.invert())))
                } catch (_: NotFoundException) {
                    return
                }
            }
            onSuccess(result.text)
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            image.close()
        }
    }
}
