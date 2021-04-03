package com.mako.mlrecognition

import android.app.Application
import com.mako.mlrecognition.di.DaggerAppComponent
import com.mako.scansdk.ScanSDK
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
class MLSdkAppImpl: Application(), HasAndroidInjector {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        initDagger()
    }

    private fun initDagger() {
        DaggerAppComponent.builder()
            .provideAppContext(this)
            .provideContext(this)
            .build()
            .inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}