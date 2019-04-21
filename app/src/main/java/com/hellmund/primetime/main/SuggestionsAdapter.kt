package com.hellmund.primetime.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.hellmund.primetime.model2.ApiMovie

internal class SuggestionsAdapter(
        fragmentMgr: FragmentManager,
        private val viewPagerHost: SuggestionFragment.ViewPagerHost
) : FragmentStatePagerAdapter(fragmentMgr) {

    var pageWidth: Float = 1f

    var movies = listOf<ApiMovie>()

    override fun getItem(position: Int): Fragment {
        return if (movies.isNotEmpty() && position == movies.lastIndex) {
            DiscoverMoreFragment.newInstance()
        } else if (movies.isNotEmpty()) {
            SuggestionFragment.newInstance(viewPagerHost, movies[position])
        } else {
            SuggestionErrorFragment.newInstance()
        }
    }

    override fun getCount(): Int = movies.size

    override fun getPageWidth(position: Int): Float = pageWidth

}
