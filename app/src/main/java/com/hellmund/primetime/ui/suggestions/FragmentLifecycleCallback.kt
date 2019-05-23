package com.hellmund.primetime.ui.suggestions

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.search.SearchFragment
import com.hellmund.primetime.ui.watchlist.WatchlistFragment

class FragmentLifecycleCallback(
        private val activity: AppCompatActivity
) : FragmentManager.FragmentLifecycleCallbacks() {

    private val searchToolbarColor: Int by lazy {
        ContextCompat.getColor(activity, R.color.adapterBackground)
    }

    private val defaultToolbarColor: Int by lazy {
        ContextCompat.getColor(activity, R.color.toolbar)
    }

    private val toolbar: Toolbar by lazy {
        activity.findViewById<Toolbar>(R.id.toolbar)
    }

    private val appBarLayout: AppBarLayout by lazy {
        activity.findViewById<AppBarLayout>(R.id.appBarLayout)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(fm.backStackEntryCount > 1)

        when (f) {
            is MainFragment, is WatchlistFragment -> activity.supportActionBar?.show()
            else -> Unit
        }

        if (f is SearchFragment) {
            toolbar.setBackgroundColor(searchToolbarColor)
            appBarLayout.elevation = 0f
        } else {
            toolbar.setBackgroundColor(defaultToolbarColor)
            appBarLayout.elevation = 11f
        }
    }

}
