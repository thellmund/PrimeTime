package com.hellmund.primetime.di

import android.app.Activity
import android.content.Context
import androidx.work.ListenableWorker
import com.hellmund.primetime.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
val Context.app: App
    get() = applicationContext as App

@FlowPreview
@ExperimentalCoroutinesApi
val Activity.injector: AppComponent
    get() = app.appComponent

@FlowPreview
@ExperimentalCoroutinesApi
val ListenableWorker.injector: AppComponent
    get() = (applicationContext as App).appComponent
