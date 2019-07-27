package com.hellmund.primetime.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        initToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.contentFrame, HistoryFragment.newInstance())
                    .commit()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        finish()
    }

}
