package com.mako.mlrecognition.di

import com.mako.mlrecognition.feature.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}
