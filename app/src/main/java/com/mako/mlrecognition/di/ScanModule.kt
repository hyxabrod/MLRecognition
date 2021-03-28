package com.mako.mlrecognition.di

import android.content.Context
import com.mako.scansdk.ScanSDK
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ScanModule {
    @Provides
    @Singleton
    fun provideScanSDK(context: Context): ScanSDK {
        return ScanSDK.init(context)
    }
}