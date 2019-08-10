package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.history.HistoryFragment
import com.hellmund.primetime.onboarding.selectgenres.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.SelectMoviesFragment
import com.hellmund.primetime.utils.NotificationUtils.createChannel
import com.hellmund.primetime.utils.NotificationUtils.scheduleNotifications
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
class App : Application(), HistoryFragment.Injector,
    SelectGenresFragment.Injector, SelectMoviesFragment.Injector {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()

        initThreeTen()
        initTimber()

        createChannel(this)
        scheduleNotifications(this)
    }

    private fun initThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    override fun injectHistoryFragment(historyFragment: HistoryFragment) {
        appComponent.inject(historyFragment)
    }

    override fun injectSelectMoviesFragment(selectMoviesFragment: SelectMoviesFragment) {
        appComponent.selectMoviesComponent().inject(selectMoviesFragment)
    }

    override fun injectSelectGenresFragment(selectGenresFragment: SelectGenresFragment) {
        appComponent.inject(selectGenresFragment)
    }

}
