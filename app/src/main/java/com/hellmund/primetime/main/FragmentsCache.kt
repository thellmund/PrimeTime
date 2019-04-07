package com.hellmund.primetime.main

import android.support.v4.app.Fragment
import android.view.MenuItem
import com.hellmund.primetime.R
import com.hellmund.primetime.search.SearchFragment
import com.hellmund.primetime.watchlist.WatchlistFragment

class FragmentsCache {

    private val fragments = mutableMapOf<MenuItem, Fragment>()

    fun getOrCreate(menuItem: MenuItem): Fragment {
        var fragment = fragments[menuItem]
        if (fragment == null) {
            fragment = createFragment(menuItem)
            put(menuItem, fragment)
        }
        return fragment
    }

    fun get(menuItem: MenuItem): Fragment? {
        return fragments[menuItem]
    }

    fun get(klass: Class<*>): Fragment? {
        return fragments.values.firstOrNull { it::class == klass }
    }

    fun put(menuItem: MenuItem, fragment: Fragment) {
        fragments[menuItem] = fragment
    }

    private fun createFragment(menuItem: MenuItem): Fragment {
        return when (menuItem.itemId) {
            R.id.home -> MainFragment.newInstance()
            R.id.search -> SearchFragment.newInstance()
            R.id.watchlist -> WatchlistFragment.newInstance()
            else -> throw IllegalStateException()
        }
    }

}

