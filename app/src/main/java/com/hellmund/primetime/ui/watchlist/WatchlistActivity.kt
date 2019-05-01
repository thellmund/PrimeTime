package com.hellmund.primetime.ui.watchlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.hellmund.primetime.R

@Deprecated("")
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
