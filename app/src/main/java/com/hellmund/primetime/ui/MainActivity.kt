package com.hellmund.primetime.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.hellmund.primetime.R
import com.hellmund.primetime.core.Intents
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import com.pandora.bottomnavigator.BottomNavigator
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

    private val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.contentFrame)

    private lateinit var navigator: BottomNavigator

    private val onNavigationItemReselected = OnNavigationItemReselectedListener {
        val reselectable = currentFragment as? Reselectable
        reselectable?.onReselected()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        // navigator.openSearch(extra)
        // bottomNavigation.selectedItemId = R.id.search
        TODO()
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
