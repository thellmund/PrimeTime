package com.hellmund.primetime.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.hellmund.primetime.R

@Deprecated("")
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
