package com.mako.mlrecognition.di

import android.app.Application
import android.content.Context
import com.mako.mlrecognition.MLSdkAppImpl
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ViewModelBindingsModule::class,
        ActivityBuilderModule::class,
        ScanModule::class
    ]
)

interface AppComponent : AndroidInjector<MLSdkAppImpl> {
    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun provideAppContext(application: Application): Builder

        @BindsInstance
        fun provideContext(context: Context): Builder
    }
}
