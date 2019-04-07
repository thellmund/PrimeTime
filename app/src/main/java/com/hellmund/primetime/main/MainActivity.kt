package com.hellmund.primetime.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.hellmund.primetime.R
import com.hellmund.primetime.search.SearchFragment
import com.hellmund.primetime.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*

class MainActivity : AppCompatActivity() {

    private val fragments = mapOf<MenuItem, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            showFragment(MainFragment.newInstance())
        }

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val fragment = fragments[menuItem] ?: createFragment(menuItem)
            showFragment(fragment)
            true
        }

        bottomNavigation.setOnNavigationItemReselectedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
            if (fragment is Reselectable) {
                fragment.onReselected()
            }
        }
    }

    private fun createFragment(menuItem: MenuItem): Fragment {
        return when (menuItem.itemId) {
            R.id.home -> MainFragment.newInstance()
            R.id.search -> SearchFragment.newInstance()
            R.id.watchlist -> WatchlistFragment.newInstance()
            else -> throw IllegalStateException()
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit()
    }

    override fun onBackPressed() {
        val fragment = fragments.values.firstOrNull { it is MainFragment } ?: MainFragment.newInstance()
        showFragment(fragment)
    }

    interface Reselectable {
        fun onReselected()
    }

}
