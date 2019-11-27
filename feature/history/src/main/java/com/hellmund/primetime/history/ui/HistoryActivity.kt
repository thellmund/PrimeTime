package com.hellmund.primetime.history.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.history.R
import com.hellmund.primetime.history.databinding.ActivityHistoryBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                replace(R.id.contentFrame, HistoryFragment.newInstance())
            }
        }
    }

    private fun initToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
        toolbar.setTitle(R.string.history)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
