package com.hellmund.primetime.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellmund.primetime.settings.R
import com.hellmund.primetime.settings.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.content, SettingsFragment.newInstance())
                .commit()
        }
    }

    private fun initToolbar() {
        binding.toolbarContainer.toolbar.apply {
            setTitle(R.string.title_activity_settings)
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { onBackPressed() }
        }
    }
}
