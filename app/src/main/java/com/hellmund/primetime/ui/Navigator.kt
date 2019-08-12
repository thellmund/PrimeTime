package com.hellmund.primetime.ui

import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.recommendations.ui.HomeFragment
import com.hellmund.primetime.search.ui.SearchFragment
import com.hellmund.primetime.watchlist.ui.WatchlistFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class Navigator(
    private val activity: FragmentActivity
) {

    private val store = ArrayMap<Int, Fragment>()

    fun open(itemId: Int) {
        val fragment = store.getOrElse(itemId) { createFragment(itemId) }
        store[itemId] = fragment
        showFragment(fragment)
    }

    fun openHome() {
        open(R.id.home)
    }

    fun openSearch(type: RecommendationsType?) {
        val fragment = SearchFragment.newInstance(type)
        store[R.id.search] = fragment
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
