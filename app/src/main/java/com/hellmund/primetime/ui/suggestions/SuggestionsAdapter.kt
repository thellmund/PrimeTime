package com.hellmund.primetime.ui.suggestions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

internal class SuggestionsAdapter(
        fragmentMgr: FragmentManager,
        private val viewPagerHost: SuggestionFragment.ViewPagerHost,
        private val onRetry: () -> Unit
) : FragmentStatePagerAdapter(fragmentMgr) {

    var pageWidth: Float = 1f
    var movies = listOf<MovieViewEntity>()

    override fun getItem(position: Int): Fragment {
        return if (movies.isNotEmpty() && position != movies.lastIndex) {
            SuggestionFragment.newInstance(movies[position], viewPagerHost)
        } else if (movies.isNotEmpty()) {
            DiscoverMoreFragment.newInstance()
        } else {
            SuggestionErrorFragment.newInstance(onRetry)
        }
    }

    override fun getCount(): Int = movies.size

    override fun getPageWidth(position: Int): Float = pageWidth

}
