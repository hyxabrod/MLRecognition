package com.mako.scansdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val TEMP_FILE_DIR = "ScannedFaceImage"

/**
 * The class takes care of file operations
 */
internal object FileUtil {
    private val TAG = FileUtil::class.java.simpleName

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * Saves bitmap to local storage
     * @param context of Application
     * @param image recognised image as Bitmap
     * @return {@link kotlinx.coroutines.flow.Flow}
     */
    fun saveBitmapToStorage(context: Context, image: Bitmap?) = flow {
        try {
            if (image == null) {
                Log.e(TAG, "Scanned image is null!!")
                emit(SaveToFileResult.Error(IllegalStateException("Scanned image is null!!")))
            }
            val cacheDir = File(context.cacheDir, TEMP_FILE_DIR)
            if (cacheDir.exists().not()) {
                cacheDir.mkdir()
            }
            val tempFile = File(cacheDir, System.currentTimeMillis().toString() + ".png")
            scope.launch(Dispatchers.IO) {
                if (!tempFile.exists()) {
                    tempFile.createNewFile()
                }

                FileOutputStream(tempFile).use { outputStream ->
                    val quality = 100
                    image?.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        outputStream
                    )
                }
            }
            emit(SaveToFileResult.FilePath(tempFile.absolutePath))
        } catch (e: IOException) {
            Log.e(TAG, "Save bitmap to file an IO exception", e)
            emit(SaveToFileResult.Error(e))
        }
    }

    sealed class SaveToFileResult {
        data class FilePath(val path: String) : SaveToFileResult()
        data class Error(val e: Throwable) : SaveToFileResult()
    }

    /**
     * Clear cashed data (temporarily created files)
     * @param context of Application
     */
    fun clearCachedData(context: Context?) {
        try {
            if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
                context?.getExternalFilesDir(TEMP_FILE_DIR)?.deleteRecursively()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Clear cachedData exception", e)
        }
    }

    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }
}