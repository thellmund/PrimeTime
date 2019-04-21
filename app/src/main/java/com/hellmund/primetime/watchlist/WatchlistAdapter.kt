package com.hellmund.primetime.watchlist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.hellmund.primetime.database.WatchlistMovie

class WatchlistAdapter(
        fragmentManager: FragmentManager,
        private val listener: WatchlistMovieFragment.OnInteractionListener
) : FragmentStatePagerAdapter(fragmentManager) {

    private val movies = mutableListOf<WatchlistMovie>()

    fun update(newMovies: List<WatchlistMovie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment {
        return WatchlistMovieFragment.newInstance(movies[position], position, listener)
    }

    override fun getCount(): Int = movies.size

}
