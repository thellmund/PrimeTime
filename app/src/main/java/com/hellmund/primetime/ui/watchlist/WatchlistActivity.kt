package com.hellmund.primetime.ui.watchlist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.hellmund.primetime.R

class WatchlistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, WatchlistFragment.newInstance())
                    .commit()
        }
    }

}
