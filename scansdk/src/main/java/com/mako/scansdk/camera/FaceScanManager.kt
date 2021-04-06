package com.mako.scansdk.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mako.scansdk.utils.imageToBitmap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private const val TAG_FACE_SCAN = "Face scan debug"

/**
 * Class manages camera and provides analyzed data
 * @param context of the application
 * @param finderView {@link PreviewView} shows camera preview
 * @param lifecycleOwner {@link LifecycleOwner}
 * @param graphicOverlay {@link GraphicOverlay}
 */
internal class FaceScanManager(
    context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay
) : ScanManager(context) {

    private var facePositionListener: FacePositionListener? = null
    override var imageCapture: ImageCapture? = null
    override var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    override var cameraProvider: ProcessCameraProvider? = null

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    override fun startCamera() {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            cameraProvider = getCameraProvider()
            if(cameraProvider?.availableCameraInfos?.size == 0){
                Log.w(TAG_FACE_SCAN, "No cameras found")
                onCameraErrorCallback()
                return@launchWhenResumed
            }

            val preview = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider(finderView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            val metrics = DisplayMetrics().also {
                finderView.display.getRealMetrics(it)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ::analyzeFaceContour)
                }

            cameraProvider?.apply {
                unbindAll()
                bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            }
        }
    }

    /**
     * Analyzes face contour
     * @param proxy {@link ImageProxy}
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun analyzeFaceContour(proxy: ImageProxy) {
        val mediaImage = proxy.image
        mediaImage?.let {
            detector.process(
                InputImage.fromMediaImage(
                    mediaImage,
                    proxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { results ->
                graphicOverlay.clear()
                results.forEach { face ->
                    graphicOverlay.add(FaceScanGraphic(
                        graphicOverlay,
                        face,
                        mediaImage.cropRect,
                        facePositionListener
                    ))
                }
                graphicOverlay.postInvalidate()
                proxy.close()
            }.addOnFailureListener {
                proxy.close()
            }
        }
    }

    override fun stopCamera() {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            cameraProvider?.unbindAll()
        }
    }

    /**
     * Set face position listener
     * @param listener {@link FacePositionListener} which returns whether the face position is correct
     */
    fun setCorrectFacePositionListener(listener: FacePositionListener){
         facePositionListener = listener
    }

    /**
     * Returns recognised image or an error as message
     * @param block lambda for result
     * @param error lambda for an error
     * */
    fun getResult(block: (Bitmap) -> Unit, error: (String?) -> Unit) {
        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.image?.let {
                        it.imageToBitmap()?.let { bitmap ->
                            block(bitmap)
                        } ?: error("Scan failed")
                    }
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    exception.printStackTrace()
                    Log.d(TAG_FACE_SCAN, exception.message ?: "A scan issue occurred")
                    error(exception.message)
                }
            })
    }
}

interface FacePositionListener{
    /**
     * Provider information whether the face position correct
     */
    fun isPositionCorrect(correct: Boolean)
}