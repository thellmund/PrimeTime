package com.hellmund.primetime.ui.suggestions

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.workers.GenresPrefetcher
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.search.SearchFragment
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.watchlist.WatchlistFragment
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.Constants.SEARCH_INTENT
import com.hellmund.primetime.utils.Constants.WATCHLIST_INTENT
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*
import javax.inject.Inject

private const val SHORTCUT_EXTRA = "intent"

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var genresRepository: GenresRepository

    @Inject
    lateinit var genresPrefetcher: GenresPrefetcher

    private val fragmentLifecycleCallback: FragmentLifecycleCallback by lazy {
        FragmentLifecycleCallback(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        injector.inject(this)

        setSupportActionBar(toolbar)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, false)

        genresPrefetcher.run()

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val fragment = createFragment(menuItem)
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

    private fun createFragment(menuItem: MenuItem): Fragment {
        return when (menuItem.itemId) {
            R.id.home -> MainFragment.newInstance()
            R.id.search -> SearchFragment.newInstance()
            R.id.watchlist -> WatchlistFragment.newInstance()
            else -> throw IllegalStateException()
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
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

        supportFragmentManager.transaction {
            replace(R.id.contentFrame, fragment)

            // TODO
            /*if (currentFragment is MainFragment) {
                addToBackStack(null)
            }*/
        }
    }

    private fun openHome() {
        val fragment = MainFragment.newInstance()
        showFragment(fragment)
        bottomNavigation.selectedItemId = R.id.home
    }

    fun openSearch(extra: String? = null) {
        val type = extra?.let { createRecommendationsTypefromIntent(it) }
        val fragment = SearchFragment.newInstance(type)
        showFragment(fragment)
        bottomNavigation.selectedItemId = R.id.search
    }

    private fun createRecommendationsTypefromIntent(intent: String): RecommendationsType {
        return when (intent) {
            Constants.NOW_PLAYING_INTENT -> RecommendationsType.NowPlaying
            Constants.UPCOMING_INTENT -> RecommendationsType.Upcoming
            else -> {
                val genre = genresRepository.getGenre(intent).blockingGet()
                val apiGenre = ApiGenre(genre.id, genre.name)
                RecommendationsType.ByGenre(apiGenre)
            }
        }
    }

    private fun openWatchlist() {
        val fragment = WatchlistFragment.newInstance()
        showFragment(fragment)
        bottomNavigation.selectedItemId = R.id.watchlist
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

}
