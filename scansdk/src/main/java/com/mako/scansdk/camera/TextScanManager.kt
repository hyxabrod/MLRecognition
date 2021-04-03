package com.mako.scansdk.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.mako.scansdk.ScanSDK
import com.mako.scansdk.utils.TextExtractor
import com.mako.scansdk.utils.takePicture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG_TEXT_SCAN = "Text scan debug"

/**
 * Class manages camera and provides analyzed data
 * @param context of the application
 * @param finderView {@link PreviewView} shows camera preview
 * @param lifecycleOwner {@link LifecycleOwner}
 */
internal class TextScanManager(
    context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
) : ScanManager(context) {
    override var imageCapture: ImageCapture? = null
    override var cameraExecutor = Executors.newSingleThreadExecutor()
    override var cameraProviderFuture: ProcessCameraProvider? = null

    /**
     * Starts camera
     */
    override fun startCamera() {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            cameraProviderFuture = getCameraProvider()

            if(cameraProviderFuture?.availableCameraInfos?.size == 0){
                Log.w(TAG_TEXT_SCAN, "No cameras found")
                onCameraErrorCallback()
                return@launchWhenResumed
            }

            val preview = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider(finderView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val metrics = DisplayMetrics().also {
                finderView.display.getRealMetrics(it)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProviderFuture?.apply {
                unbindAll()
                bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            }
        }
    }

    /**
     * Stops camera
     */
    override fun stopCamera() {
        cameraProviderFuture?.unbindAll()
    }

    /**
     * Starts text recognition and provides result to subscriber
     * @param block provides the result of the scanning
     * @param error provides error message
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    fun scanAndAnalyzeText(block: (String) -> Unit, error: (String?) -> Unit) {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            imageCapture?.takePicture(cameraExecutor)?.apply {
                val imageInput = InputImage.fromMediaImage(
                    requireNotNull(image),
                    imageInfo.rotationDegrees
                )
                TextRecognition.getClient().process(imageInput).addOnCompleteListener {
                    val result = TextExtractor.extractAllTexts(it.result)
                    val text = result.joinToString("\n") { it }
                    if (text.isNotEmpty()) {
                        block(text)
                    } else {
                        error("No texts recognized")
                    }
                    close()
                }.addOnFailureListener {
                    it.printStackTrace()
                    Log.d(TAG_TEXT_SCAN, it.message ?: "A scan issue occurred")
                    error(it.message)
                    close()
                }
            }
        }
    }
}



