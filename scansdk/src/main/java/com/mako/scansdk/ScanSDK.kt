package com.mako.scansdk

import android.content.Context
import android.util.Log
import com.mako.scansdk.utils.FileUtil
import java.lang.IllegalStateException

/**
 * Interface of ScanSDK Impl
 */
interface ScanSDK {
    /**
     * Clear cashed data (temporarily created files)
     */
    fun clearCachedData()

    companion object {
        private var instance: ScanSDK? = null

        /**
         * Creates single instance of ScanSDK
         * @param context of Application
         */
        fun init(context: Context): ScanSDK {
            if (instance == null) {
                instance = ScanSDKImpl(context)
            }
            return instance!!
        }
    }
}

/**
 * Implementation of ScanSDK
 * @param context of Application
 */
internal class ScanSDKImpl(private val context: Context?) : ScanSDK {
    private val TAG = ScanSDKImpl::class.java.simpleName

    override fun clearCachedData() {
        if (context == null) {
            val errorMessage = "Context is not initialized!"
            Log.e(TAG, errorMessage)
            throw IllegalStateException(errorMessage)
        }
        FileUtil.clearCachedData(context)
    }
}
