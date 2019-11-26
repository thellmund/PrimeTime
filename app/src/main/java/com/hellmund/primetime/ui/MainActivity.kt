package com.hellmund.primetime.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.hellmund.primetime.R
import com.hellmund.primetime.core.Intents
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import com.hellmund.primetime.workers.GenresPrefetcher
import com.pandora.bottomnavigator.BottomNavigator
import kotlinx.android.synthetic.main.activity_main.bottomNavigation
import javax.inject.Inject
import javax.inject.Provider

private const val SHORTCUT_EXTRA = "intent"

class MainActivity : AppCompatActivity() {

    private val fragmentCallback: FragmentLifecycleCallback by lazy {
        FragmentLifecycleCallback(this)
    }

    private val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.contentFrame)

    private lateinit var navigator: BottomNavigator

    private val onNavigationItemReselected = OnNavigationItemReselectedListener {
        val reselectable = currentFragment as? Reselectable
        reselectable?.onReselected()
    }

    @Inject
    lateinit var genresPrefetcher: GenresPrefetcher

    @Inject
    lateinit var viewModelProvider: Provider<MainViewModel>

    private val viewModel: MainViewModel by lazyViewModel { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerAppComponent.builder()
            .coreComponent(coreComponent)
            .build()
            .inject(this)

        genresPrefetcher.run()

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallback, false)
        bottomNavigation.setOnNavigationItemReselectedListener(onNavigationItemReselected)

        navigator = BottomNavigator.onCreate(
            activity = this,
            rootFragmentsFactory = mapOf(
                R.id.home to { HomeFragment.newInstance() },
                R.id.search to { SearchFragment.newInstance() },
                R.id.watchlist to { WatchlistFragment.newInstance() }
            ),
            defaultTab = R.id.home,
            fragmentContainer = R.id.contentFrame,
            bottomNavigationView = bottomNavigation
        )

        viewModel.watchlistCount.observe(this, this::updateWatchlistBadge)

        intent?.getStringExtra(SHORTCUT_EXTRA)?.let {
            handleShortcutOpen(it)
        }
    }

    private fun updateWatchlistBadge(count: Int) {
        if (count > 0) {
            val badgeDrawable = bottomNavigation.showBadge(R.id.watchlist)
            badgeDrawable.backgroundColor = ContextCompat.getColor(this, R.color.teal_500)
            badgeDrawable.number = count
        } else {
            bottomNavigation.removeBadge(R.id.watchlist)
        }
    }

    private fun handleShortcutOpen(intent: String) {
        when (intent) {
            Intents.WATCHLIST -> openWatchlistFromIntent()
            Intents.SEARCH -> openSearchFromIntent()
            else -> openSearchFromIntent(intent)
        }
    }

    private fun openSearchFromIntent(extra: String? = null) {
        navigator.switchTab(R.id.search)

        if (extra != null) {
            val searchFragment = navigator.currentFragment() as? SearchFragment
            searchFragment?.openCategory(extra)
        }
    }

    private fun openWatchlistFromIntent() {
        navigator.switchTab(R.id.watchlist)
    }

    override fun onBackPressed() {
        if (!navigator.pop()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentCallback)
        super.onDestroy()
    }
}
