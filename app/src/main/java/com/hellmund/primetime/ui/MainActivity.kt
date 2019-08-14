package com.hellmund.primetime.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.hellmund.primetime.R
import com.hellmund.primetime.core.Intents
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import kotlinx.android.synthetic.main.activity_main.bottomNavigation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

private const val SHORTCUT_EXTRA = "intent"

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {

    private val fragmentCallback: FragmentLifecycleCallback by lazy {
        FragmentLifecycleCallback(this)
    }

    private val navigator = TabsNavigator(this)

    private val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.contentFrame)

    private val onNavigationItemSelected = { menuItem: MenuItem ->
        navigator.open(menuItem.itemId)
        true
    }

    private val onNavigationItemReselected = OnNavigationItemReselectedListener {
        val reselectable = currentFragment as? Reselectable
        reselectable?.onReselected()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallback, false)
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelected)
        bottomNavigation.setOnNavigationItemReselectedListener(onNavigationItemReselected)

        if (savedInstanceState == null) {
            navigator.openHome()
        }

        intent?.getStringExtra(SHORTCUT_EXTRA)?.let {
            handleShortcutOpen(it)
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
        navigator.openSearch(extra)
        bottomNavigation.selectedItemId = R.id.search
    }

    private fun openWatchlistFromIntent() {
        navigator.openWatchlist()
        bottomNavigation.selectedItemId = R.id.watchlist
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
            adjustBottomNavigation()
        } else {
            super.onBackPressed()
        }
    }

    private fun adjustBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(null)
        bottomNavigation.setOnNavigationItemReselectedListener(null)
        bottomNavigation.selectedItemId = when (currentFragment) {
            is HomeFragment -> R.id.home
            is SearchFragment -> R.id.search
            is WatchlistFragment -> R.id.watchlist
            else -> throw IllegalStateException()
        }
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelected)
        bottomNavigation.setOnNavigationItemReselectedListener(onNavigationItemReselected)
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentCallback)
        super.onDestroy()
    }

}
