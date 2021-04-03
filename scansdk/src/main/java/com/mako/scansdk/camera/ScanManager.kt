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
    abstract var imageCapture: ImageCapture?
    abstract var cameraExecutor: ExecutorService
    abstract var cameraProviderFuture: ProcessCameraProvider?
    protected lateinit var onCameraErrorCallback: () -> Unit

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
    suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(context).apply {
            addListener({
                continuation.resume(get())
            }, cameraExecutor)
        }
    }

    /**
     * Error camera callback
     * @param block error callback
     */
    fun setCameraErrorCallback(block: () -> Unit) {
        onCameraErrorCallback = block
    }
}