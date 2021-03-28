package com.mako.scansdk.camera

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Basic class for scan managers
 * @param context of the application
 */
internal abstract class ScanManager(
    private val context: Context
) : LifecycleObserver {
    internal abstract var imageCapture: ImageCapture?
    internal abstract var cameraExecutor: ExecutorService
    internal abstract var cameraProviderFuture: ProcessCameraProvider?

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun connectListener() {
        startCamera()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun disconnectListener() {
        stopCamera()
    }

    /**
     * Starts camera
     */
    abstract fun startCamera()

    /**
     * Stops camera
     */
    abstract fun stopCamera()

    /**
     * Asynchronously returns camera provider
     * @return {@link ProcessCameraProvider}
     */
    internal suspend fun getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(context).apply {
                addListener(Runnable {
                    continuation.resume(get())
                }, cameraExecutor)
            }
        }
}