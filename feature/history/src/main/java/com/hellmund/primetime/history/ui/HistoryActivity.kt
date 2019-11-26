package com.hellmund.primetime.history.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellmund.primetime.history.R
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
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
        toolbar.setTitle(R.string.history)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
