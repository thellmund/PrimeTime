package com.hellmund.primetime.ui.watchlist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.hellmund.primetime.ui.watchlist.details.WatchlistMovieFragment

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
        return WatchlistMovieFragment.newInstance(movies[position], listener)
    }

    override fun getCount(): Int = movies.size

}
