package com.hellmund.primetime.settings.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.hellmund.primetime.settings.R
import com.hellmund.primetime.settings.databinding.ActivitySettingsBinding
import com.hellmund.primetime.ui_common.util.requestFullscreenLayout
import dev.chrisbanes.insetter.doOnApplyWindowInsets

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        window.requestFullscreenLayout()

//        binding.contentFrame.doOnApplyWindowInsets { v, insets, initialState ->
//            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = initialState.margins.top + insets.systemWindowInsetTop
//            }
//        }

        binding.toolbarContainer.root.doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = initialState.margins.top + insets.systemWindowInsetTop
            }
        }

        binding.contentFrame.doOnApplyWindowInsets { v, insets, initialState ->
            v.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.contentFrame, SettingsFragment.newInstance())
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
