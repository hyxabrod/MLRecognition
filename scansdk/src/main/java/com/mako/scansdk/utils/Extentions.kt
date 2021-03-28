package com.mako.scansdk.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun ImageCapture.takePicture(executor: Executor): ImageProxy {

    return suspendCoroutine { continuation ->
        takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {

            override fun onCaptureSuccess(image: ImageProxy) {
                continuation.resume(image)
                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                continuation.resumeWithException(exception)
                exception.printStackTrace()
                super.onError(exception)
            }
        })
    }
}

/**
 * Convert {@link Image} to {@link Bitmap}
 * @return {@link Bitmap}
 */
fun Image.imageToBitmap(): Bitmap? {
    val buffer = this.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

/**
 * Rotates {@link Bitmap}
 * @return rotated {@link Bitmap}
 */
fun Bitmap.rotateFlipImage(): Bitmap? {
    val matrix = Matrix().apply {
        preScale(-1.0f, 1.0f)
        postRotate(90f)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

