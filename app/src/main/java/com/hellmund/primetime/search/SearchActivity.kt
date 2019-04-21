package com.hellmund.primetime.search

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.hellmund.primetime.R

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, SearchFragment.newInstance(null))
                    .commit()
        }
    }

}
