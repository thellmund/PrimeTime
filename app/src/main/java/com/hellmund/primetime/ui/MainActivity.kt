package com.hellmund.primetime.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.hellmund.primetime.R
import com.hellmund.primetime.core.Intents
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.databinding.ActivityMainBinding
import com.hellmund.primetime.di.DaggerAppComponent
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.util.applyExitMaterialTransform
import com.hellmund.primetime.ui_common.util.requestFullscreenLayout
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import com.hellmund.primetime.workers.GenresPrefetcher
import com.pandora.bottomnavigator.BottomNavigator
import dev.chrisbanes.insetter.doOnApplyWindowInsets
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

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyExitMaterialTransform()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        requestFullscreenLayout()

        DaggerAppComponent.builder()
            .coreComponent(coreComponent)
            .build()
            .inject(this)

        genresPrefetcher.run()
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallback, false)

        setupNavigation()
        observeWatchlistCount()
        adjustToWindowInsets()

        intent?.getStringExtra(SHORTCUT_EXTRA)?.let {
            handleShortcutOpen(it)
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnNavigationItemReselectedListener(onNavigationItemReselected)
        navigator = BottomNavigator.onCreate(
            activity = this,
            rootFragmentsFactory = mapOf(
                R.id.home to { HomeFragment.newInstance() },
                R.id.search to { SearchFragment.newInstance() },
                R.id.watchlist to { WatchlistFragment.newInstance() }
            ),
            defaultTab = R.id.home,
            fragmentContainer = R.id.contentFrame,
            bottomNavigationView = binding.bottomNavigation
        )
    }

    private fun observeWatchlistCount() {
        viewModel.watchlistCount.observe(this, this::updateWatchlistBadge)
    }

    private fun adjustToWindowInsets() {
        binding.contentFrame.doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = initialState.margins.top + insets.systemWindowInsetTop
            }
        }

        binding.bottomNavigation.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }
    }

    private fun updateWatchlistBadge(count: Int) {
        val bottomNavigation = binding.bottomNavigation
        if (count > 0) {
            val badgeDrawable = bottomNavigation.getOrCreateBadge(R.id.watchlist)
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
