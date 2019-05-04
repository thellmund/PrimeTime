package com.hellmund.primetime.ui.watchlist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.hellmund.primetime.ui.watchlist.details.WatchlistMovieFragment

class WatchlistAdapter(
        fragmentManager: FragmentManager,
        private val listener: WatchlistMovieFragment.OnInteractionListener,
        private val movies: List<WatchlistMovieViewEntity> = listOf()
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return WatchlistMovieFragment.newInstance(movies[position], listener)
    }

    override fun getCount(): Int = movies.size

}
