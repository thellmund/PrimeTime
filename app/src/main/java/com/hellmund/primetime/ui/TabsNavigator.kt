package com.hellmund.primetime.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.R
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class TabsNavigator(
    private val activity: FragmentActivity
) {

    fun open(itemId: Int) {
        val fragment = createFragment(itemId)
        showFragment(fragment)
    }

    fun openHome() {
        open(R.id.home)
    }

    fun openSearch(extra: String? = null) {
        val fragment = SearchFragment.newInstance(extra)
        showFragment(fragment)
    }

    fun openWatchlist() {
        open(R.id.watchlist)
    }

    private fun createFragment(
        itemId: Int
    ): Fragment {
        return when (itemId) {
            R.id.home -> HomeFragment.newInstance()
            R.id.search -> SearchFragment.newInstance()
            R.id.watchlist -> WatchlistFragment.newInstance()
            else -> throw IllegalStateException("Invalid menu item $itemId")
        }
    }

    private fun showFragment(fragment: Fragment) {
        activity.supportFragmentManager.transaction {
            replace(R.id.contentFrame, fragment)
        }
    }

}
