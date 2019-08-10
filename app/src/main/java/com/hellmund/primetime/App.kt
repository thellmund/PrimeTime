package com.hellmund.primetime

import android.app.Application
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.di.AppComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.history.ui.HistoryFragment
import com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.settings.ui.SettingsFragment
import com.hellmund.primetime.notifications.NotificationUtils.createChannel
import com.hellmund.primetime.notifications.NotificationUtils.scheduleNotifications
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
class App : Application(), HistoryFragment.Injector, SelectGenresFragment.Injector,
    SelectMoviesFragment.Injector, SettingsFragment.Injector,
    WatchlistFragment.Injector, SearchFragment.Injector,
    MovieDetailsFragment.Injector, HomeFragment.Injector {

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

    override fun injectSettingsFragment(settingsFragment: SettingsFragment) {
        appComponent.inject(settingsFragment)
    }

    override fun injectWatchlistFragment(watchlistFragment: WatchlistFragment) {
        appComponent.inject(watchlistFragment)
    }

    override fun injectSearchFragment(searchFragment: SearchFragment) {
        appComponent.inject(searchFragment)
    }

    override fun injectMovieDetailsFragment(
        movieDetailsFragment: MovieDetailsFragment,
        movieViewEntity: MovieViewEntity
    ) {
        appComponent.movieDetailsComponent()
            .create(movieViewEntity)
            .inject(movieDetailsFragment)
    }

    override fun injectHomeFragment(homeFragment: HomeFragment, type: RecommendationsType) {
        appComponent.mainComponent()
            .create(type)
            .inject(homeFragment)
    }

}
