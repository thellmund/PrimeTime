package com.hellmund.primetime.di

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.hellmund.primetime.App

val Context.app: App
    get() = applicationContext as App

val Activity.injector: AppComponent
    get() = app.appComponent

val Fragment.injector: AppComponent
    get() = requireContext().app.appComponent
