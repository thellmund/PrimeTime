package com.hellmund.primetime.ui.watchlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class WatchlistAdapter(
        fragmentManager: FragmentManager,
        private val listener: WatchlistMovieFragment.OnInteractionListener
) : FragmentStatePagerAdapter(fragmentManager) {

    private val movies = mutableListOf<WatchlistMovieViewEntity>()

    fun update(newMovies: List<WatchlistMovieViewEntity>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment {
        return WatchlistMovieFragment.newInstance(movies[position], position, listener)
    }

    override fun getCount(): Int = movies.size

}
