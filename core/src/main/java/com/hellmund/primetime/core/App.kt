package com.hellmund.primetime.core

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.ListenableWorker
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.core.di.DaggerCoreComponent
import com.hellmund.primetime.core.notifications.NotificationUtils.Companion.createChannel
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
class App : Application() {

    private val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        initThreeTen()
        initTimber()
        createChannel(this)
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        fun coreComponent(context: Context) = (context.applicationContext as App).coreComponent
    }
}

val Context.coreComponent: CoreComponent get() = App.coreComponent(this)
val Fragment.coreComponent: CoreComponent get() = App.coreComponent(requireContext())
val ListenableWorker.coreComponent: CoreComponent get() = App.coreComponent(applicationContext)
