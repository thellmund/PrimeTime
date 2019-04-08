package com.hellmund.primetime.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.hellmund.primetime.R
import com.hellmund.primetime.search.SearchFragment
import com.hellmund.primetime.utils.Constants.SEARCH_INTENT
import com.hellmund.primetime.utils.Constants.WATCHLIST_INTENT
import com.hellmund.primetime.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*

class MainActivity : AppCompatActivity() {

    private val fragmentLifecycleCallback: FragmentLifecycleCallback by lazy {
        FragmentLifecycleCallback(this)
    }

    private val fragmentsCache = FragmentsCache()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, false)

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val fragment = fragmentsCache.getOrCreate(menuItem)
            showFragment(fragment)
            true
        }

        bottomNavigation.setOnNavigationItemReselectedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
            if (fragment is Reselectable) {
                fragment.onReselected()
            }
        }

        if (savedInstanceState == null) {
            showFragment(MainFragment.newInstance())
        }

        intent?.getStringExtra(SHORTCUT_EXTRA)?.let {
            handleShortcutOpen(it)
        }
    }

    private fun handleShortcutOpen(intent: String) {
        when (intent) {
            WATCHLIST_INTENT -> openWatchlist()
            SEARCH_INTENT -> openSearch()
            else -> openSearch(intent)
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit()
    }

    private fun openHome() {
        val fragment = findFragment(MainFragment::class.java) ?: MainFragment.newInstance()
        bottomNavigation.selectedItemId = R.id.home
        showFragment(fragment)
    }

    fun openSearch(extra: String? = null) {
        val fragment = if (extra != null) {
            val recommendationsType = RecommendationsType.fromIntent(this, extra)
            SearchFragment.newInstance(recommendationsType)
        } else {
            findFragment(SearchFragment::class.java) ?: SearchFragment.newInstance()
        }

        // val fragment = findFragment(SearchFragment::class.java) ?: SearchFragment.newInstance(extra)
        bottomNavigation.selectedItemId = R.id.search
        showFragment(fragment)
    }

    private fun openWatchlist() {
        val fragment = findFragment(WatchlistFragment::class.java) ?: WatchlistFragment.newInstance()
        bottomNavigation.selectedItemId = R.id.watchlist
        showFragment(fragment)
    }

    private fun findFragment(klass: Class<*>): Fragment? {
        return fragmentsCache.get(klass)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            openHome()
        }
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallback)
        super.onDestroy()
    }

    interface Reselectable {
        fun onReselected()
    }

    companion object {
        private const val SHORTCUT_EXTRA = "intent"
    }

}
